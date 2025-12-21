package vn.edu.ueh.socialapplication.home;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

import vn.edu.ueh.socialapplication.R;
import vn.edu.ueh.socialapplication.auth.LoginActivity;
import vn.edu.ueh.socialapplication.chat.ChatListActivity;
import vn.edu.ueh.socialapplication.data.model.Post;
import vn.edu.ueh.socialapplication.data.model.User;
import vn.edu.ueh.socialapplication.data.repository.UserRepository;
import vn.edu.ueh.socialapplication.notification.NotificationActivity;
import vn.edu.ueh.socialapplication.post.CreatePostActivity;
import vn.edu.ueh.socialapplication.post.EditPostActivity;
import vn.edu.ueh.socialapplication.post.PostAdapter;
import vn.edu.ueh.socialapplication.post.PostDetailActivity;
import vn.edu.ueh.socialapplication.profile.EditProfileActivity;
import vn.edu.ueh.socialapplication.profile.ProfileActivity;
import vn.edu.ueh.socialapplication.search.SearchActivity;
import vn.edu.ueh.socialapplication.utils.ImageUtils;

public class HomeActivity extends AppCompatActivity implements PostAdapter.OnPostClickListener {
    private RecyclerView rvPosts;
    private PostAdapter postAdapter;
    private HomeViewModel homeViewModel;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayoutManager layoutManager;

    private ImageView profileImage;
    private ImageView addPostIcon;
    private ImageView searchIcon;
    private ImageView notificationIcon;
    private ImageView chatIcon;
    private ProgressBar progressBar;
    private TextView noResultsText;
    private UserRepository userRepository;
    private ListenerRegistration notificationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        profileImage = findViewById(R.id.profile_image);
        addPostIcon = findViewById(R.id.add_post_icon);
        searchIcon = findViewById(R.id.search_icon);
        notificationIcon = findViewById(R.id.notification_icon);
        chatIcon = findViewById(R.id.chat_icon);
        progressBar = findViewById(R.id.progress_bar_home);
        noResultsText = findViewById(R.id.no_results_text_home);

        userRepository = new UserRepository();

        rvPosts = findViewById(R.id.rvPosts);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        layoutManager = new LinearLayoutManager(this);
        rvPosts.setLayoutManager(layoutManager);

        postAdapter = new PostAdapter(this, new ArrayList<>(), this);
        rvPosts.setAdapter(postAdapter);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        observeViewModel();
        setupListeners();

        swipeRefreshLayout.setOnRefreshListener(() -> homeViewModel.refreshFeed());

        rvPosts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 2
                            && firstVisibleItemPosition >= 0) {
                        homeViewModel.loadMore();
                    }
                }
            }
        });

        swipeRefreshLayout.setRefreshing(true);
        homeViewModel.refreshFeed();
    }

    private void observeViewModel() {
        homeViewModel.getPostsData().observe(this, posts -> {
            if (posts != null) {
                postAdapter.setPosts(posts);
            }
        });

        homeViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading && layoutManager.findFirstVisibleItemPosition() <= 0) {
                swipeRefreshLayout.setRefreshing(true);
            } else if (!isLoading) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        homeViewModel.getDeletePostStatus().observe(this, isSuccess -> {
            if (isSuccess != null && isSuccess) {
                Toast.makeText(this, "Post deleted", Toast.LENGTH_SHORT).show();
                homeViewModel.refreshFeed();
            } else if (isSuccess != null) {
                Toast.makeText(this, "Failed to delete post", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCommentClick(Post post) {
        if (post.getPostId() == null || post.getPostId().isEmpty()) {
            Toast.makeText(this, "Lỗi: Bài viết chưa có ID!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(HomeActivity.this, PostDetailActivity.class);
        intent.putExtra("post_object", post);
        startActivity(intent);
    }

    @Override
    public void onEditClick(Post post) {
        Intent intent = new Intent(this, EditPostActivity.class);
        intent.putExtra("post_id", post.getPostId());
        intent.putExtra("post_content", post.getContent());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Post post) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Delete", (dialog, which) -> homeViewModel.deletePost(post.getPostId()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCurrentUser();
        listenForNotifications();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (notificationListener != null) {
            notificationListener.remove();
        }
    }

    private void setupListeners() {
        if (profileImage != null) {
            profileImage.setOnClickListener(this::showProfileMenu);
        }

        if (searchIcon != null) {
            searchIcon.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
                startActivity(intent);
            });
        }

        if (addPostIcon != null) {
            addPostIcon.setOnClickListener(v -> {
                startActivity(new Intent(HomeActivity.this, CreatePostActivity.class));
            });
        }

        if (notificationIcon != null) {
            notificationIcon.setOnClickListener(v -> {
                startActivity(new Intent(HomeActivity.this, NotificationActivity.class));
            });
        }

        if (chatIcon != null) {
            chatIcon.setOnClickListener(v -> {
                startActivity(new Intent(HomeActivity.this, ChatListActivity.class));
            });
        }
    }

    private void showProfileMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenuInflater().inflate(R.menu.profile_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                // Redirect to login if user is not authenticated
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return true;
            }

            if (itemId == R.id.menu_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            } else if (itemId == R.id.menu_account) {
                startActivity(new Intent(this, EditProfileActivity.class));
                return true;
            } else if (itemId == R.id.menu_logout) {
                logoutUser();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void loadCurrentUser() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            userRepository.getUser(uid, new UserRepository.OnUserLoadedListener() {
                @Override
                public void onUserLoaded(User user) {
                    updateUI(user);
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(HomeActivity.this, "Không tìm thấy hồ sơ người dùng.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void updateUI(User user) {
        if (user != null && user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            ImageUtils.loadImage(user.getAvatar(), profileImage);
        } else if (profileImage != null) {
            profileImage.setImageResource(R.drawable.ic_account_circle);
        }
    }

    private void listenForNotifications() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Query query = FirebaseFirestore.getInstance().collection("notifications")
                    .whereEqualTo("userId", currentUser.getUid())
                    .whereEqualTo("read", false);

            notificationListener = query.addSnapshotListener((snapshots, e) -> {
                if (e != null) {
                    Log.w("HomeActivity", "Listen failed.", e);
                    return;
                }

                if (snapshots != null && !snapshots.isEmpty()) {
                    notificationIcon.setColorFilter(Color.RED);
                } else {
                    notificationIcon.clearColorFilter();
                }
            });
        }
    }
}

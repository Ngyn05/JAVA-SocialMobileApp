package vn.edu.ueh.socialapplication.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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

import java.util.ArrayList;
import java.util.List;

import vn.edu.ueh.socialapplication.post.CreatePostActivity;
import vn.edu.ueh.socialapplication.profile.EditProfileActivity;
import vn.edu.ueh.socialapplication.utils.ImageUtils;
import vn.edu.ueh.socialapplication.auth.LoginActivity;
import vn.edu.ueh.socialapplication.profile.ProfileActivity;
import vn.edu.ueh.socialapplication.R;
import vn.edu.ueh.socialapplication.search.UserAdapter;
import vn.edu.ueh.socialapplication.data.repository.UserRepository;
import vn.edu.ueh.socialapplication.data.model.Post;
import vn.edu.ueh.socialapplication.data.model.User;
import vn.edu.ueh.socialapplication.post.PostAdapter;
import vn.edu.ueh.socialapplication.post.PostDetailActivity;

public class HomeActivity extends AppCompatActivity implements PostAdapter.OnPostClickListener {
    private RecyclerView rvPosts;
    private PostAdapter postAdapter;
    private HomeViewModel homeViewModel;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayoutManager layoutManager;

    private UserAdapter userAdapter;
    private List<User> userList;
    private ImageView profileImage;
    private ImageView addPostIcon;
    private EditText searchBar;
    private ProgressBar progressBar;
    private TextView noResultsText;
    private UserRepository userRepository;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private static final long DEBOUNCE_DELAY = 500; // 500ms

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        profileImage = findViewById(R.id.profile_image);
        addPostIcon = findViewById(R.id.add_post_icon);
        searchBar = findViewById(R.id.search_bar_home);
        progressBar = findViewById(R.id.progress_bar_home);
        noResultsText = findViewById(R.id.no_results_text_home);

        userRepository = new UserRepository();

        // Setup RecyclerView

        userList = new ArrayList<>();
        userAdapter = new UserAdapter(this, userList);

        setupListeners();

        // --- ÁNH XẠ VIEW ---
        rvPosts = findViewById(R.id.rvPosts);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        
        layoutManager = new LinearLayoutManager(this);
        rvPosts.setLayoutManager(layoutManager);

        // Khởi tạo adapter
        postAdapter = new PostAdapter(new ArrayList<>(), this);
        rvPosts.setAdapter(postAdapter);

        // --- KHỞI TẠO VIEWMODEL ---
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // --- LẮNG NGHE DỮ LIỆU ---
        observeViewModel();

        // --- SETUP SWIPE TO REFRESH ---
        swipeRefreshLayout.setOnRefreshListener(() -> {
            homeViewModel.refreshFeed();
        });

        // --- SETUP PAGINATION SCROLL LISTENER ---
        rvPosts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                // dy > 0 nghĩa là đang cuộn xuống
                if (dy > 0) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    // Kiểm tra nếu đã cuộn gần đến cuối (còn 2 item nữa là hết)
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 2
                            && firstVisibleItemPosition >= 0) {
                        Log.d("HomeActivity", "Cuộn đến cuối, đang tải thêm...");
                        homeViewModel.loadMore();
                    }
                }
            }
        });

        // --- TẢI DỮ LIỆU BAN ĐẦU ---
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
            // Chỉ hiển thị loading ở SwipeRefresh khi đang refresh (trang đầu)
            // Nếu là load more, bạn có thể thêm một ProgressBar ở dưới cùng RecyclerView
            if (isLoading && layoutManager.findFirstVisibleItemPosition() <= 0) {
                swipeRefreshLayout.setRefreshing(true);
            } else if (!isLoading) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        homeViewModel.getIsLastPage().observe(this, isLastPage -> {
            if (isLastPage) {
                Log.d("HomeActivity", "Đã tải hết tất cả bài viết.");
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
    protected void onResume() {
        super.onResume();
        loadCurrentUser();
    }

    private void setupListeners() {
        profileImage.setOnClickListener(this::showProfileMenu);

        addPostIcon.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, CreatePostActivity.class));
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchRunnable = () -> searchUsers(s.toString());
                searchHandler.postDelayed(searchRunnable, DEBOUNCE_DELAY);
            }
        });
    }

    private void searchUsers(String query) {
        if (query.isEmpty()) {
            userList.clear();
            userAdapter.notifyDataSetChanged();
            noResultsText.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        noResultsText.setVisibility(View.GONE);

        userRepository.searchUsers(query, new UserRepository.OnUsersSearchedListener() {
            @Override
            public void onUsersSearched(List<User> users) {
                progressBar.setVisibility(View.GONE);
                userList.clear();
                userList.addAll(users);
                userAdapter.notifyDataSetChanged();

                if (users.isEmpty()) {
                    noResultsText.setVisibility(View.VISIBLE);
                } else {
                    noResultsText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(Exception e) {
                progressBar.setVisibility(View.GONE);
                noResultsText.setVisibility(View.VISIBLE);
                noResultsText.setText("Lỗi khi tìm kiếm.");
            }
        });
    }

    private void showProfileMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenuInflater().inflate(R.menu.profile_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
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
                    Toast.makeText(vn.edu.ueh.socialapplication.home.HomeActivity.this, "Không tìm thấy hồ sơ người dùng. Đang đăng xuất.", Toast.LENGTH_LONG).show();
                    logoutUser();
                }
            });
        }
    }

    private void updateUI(User user) {
        if (user != null && user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            ImageUtils.loadImage(user.getAvatar(), profileImage);
        } else {
            profileImage.setImageResource(R.drawable.ic_account_circle);
        }
    }
}

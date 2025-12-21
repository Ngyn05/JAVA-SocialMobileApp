package vn.edu.ueh.socialapplication.profile;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import vn.edu.ueh.socialapplication.R;
import vn.edu.ueh.socialapplication.chat.ChatActivity;
import vn.edu.ueh.socialapplication.data.model.Post;
import vn.edu.ueh.socialapplication.data.model.User;
import vn.edu.ueh.socialapplication.post.EditPostActivity;
import vn.edu.ueh.socialapplication.post.PostAdapter;
import vn.edu.ueh.socialapplication.post.PostDetailActivity;
import vn.edu.ueh.socialapplication.utils.ImageUtils;

public class OtherProfileActivity extends AppCompatActivity implements PostAdapter.OnPostClickListener {

    private CircleImageView imageProfile;
    private TextView postsCount, followersCount, followingCount, fullname, bio, toolbarTitle;
    private MaterialButton btnFollow;
    private ImageButton btnChat;
    private ImageView backButton;
    private RecyclerView recyclerViewPosts;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private LinearLayout followersLayout, followingLayout;

    private ProfileViewModel profileViewModel;
    private OtherProfileViewModel otherProfileViewModel;
    private String profileId;
    private String currentUserId;
    private boolean isFollowing = false;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        profileId = getIntent().getStringExtra("USER_ID");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Redirect to own profile if viewing self
        if (profileId != null && profileId.equals(currentUserId)) {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_other_profile);


        if (profileId == null) {
            finish();
            return;
        }

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        otherProfileViewModel = new ViewModelProvider(this).get(OtherProfileViewModel.class);

        initViews();
        observeViewModel();
        setupListeners();

        profileViewModel.loadUserProfile(profileId);
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        imageProfile = findViewById(R.id.image_profile);
        postsCount = findViewById(R.id.posts_count);
        followersCount = findViewById(R.id.followers_count);
        followingCount = findViewById(R.id.following_count);
        fullname = findViewById(R.id.fullname);
        bio = findViewById(R.id.bio);
        btnFollow = findViewById(R.id.btn_follow);
        btnChat = findViewById(R.id.btn_chat);
        recyclerViewPosts = findViewById(R.id.recycler_view_posts);
        toolbarTitle = findViewById(R.id.toolbar_title);
        followersLayout = findViewById(R.id.followers_layout);
        followingLayout = findViewById(R.id.following_layout);
        backButton = findViewById(R.id.back_button_other_profile);

        recyclerViewPosts.setHasFixedSize(true);
        recyclerViewPosts.setLayoutManager(new LinearLayoutManager(this));
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(this, postList, this);
        recyclerViewPosts.setAdapter(postAdapter);
    }

    private void observeViewModel() {
        profileViewModel.getUser().observe(this, user -> {
            if (user != null) {
                currentUser = user;
                if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                    ImageUtils.loadImage(user.getAvatar(), imageProfile);
                }
                fullname.setText(user.getUserName());
                toolbarTitle.setText(user.getUserName());
                bio.setText(user.getBio());

                if (user.getFollowers() != null && user.getFollowers().contains(currentUserId)) {
                    isFollowing = true;
                } else {
                    isFollowing = false;
                }
                updateFollowButtonState();
            }
        });

        profileViewModel.getFollowersCount().observe(this, count -> followersCount.setText(String.valueOf(count)));
        profileViewModel.getFollowingCount().observe(this, count -> followingCount.setText(String.valueOf(count)));

        profileViewModel.getUserPosts().observe(this, posts -> {
            if (posts != null) {
                postAdapter.setPosts(posts);
                postsCount.setText(String.valueOf(posts.size()));
            }
        });

        otherProfileViewModel.getError().observe(this, errorMsg -> {
            if (errorMsg != null) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                // If there's an error, revert the state
                isFollowing = !isFollowing;
                updateFollowButtonState();
                updateFollowerCount(isFollowing);
            }
        });
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        btnFollow.setOnClickListener(v -> {
            isFollowing = !isFollowing;
            updateFollowButtonState();
            updateFollowerCount(isFollowing);

            if (isFollowing) {
                otherProfileViewModel.followUser(currentUserId, profileId);
            } else {
                otherProfileViewModel.unfollowUser(currentUserId, profileId);
            }
        });

        btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(OtherProfileActivity.this, ChatActivity.class);
            intent.putExtra("otherUserId", profileId);
            if (currentUser != null) {
                intent.putExtra("otherUsername", currentUser.getUserName());
            }
            startActivity(intent);
        });

        followersLayout.setOnClickListener(v -> {
            if (currentUser != null && currentUser.getFollowers() != null && !currentUser.getFollowers().isEmpty()) {
                Intent intent = new Intent(this, FollowListActivity.class);
                intent.putExtra("title", "Followers");
                intent.putStringArrayListExtra("userIds", new ArrayList<>(currentUser.getFollowers()));
                startActivity(intent);
            }
        });

        followingLayout.setOnClickListener(v -> {
            if (currentUser != null && currentUser.getFollowing() != null && !currentUser.getFollowing().isEmpty()) {
                Intent intent = new Intent(this, FollowListActivity.class);
                intent.putExtra("title", "Following");
                intent.putStringArrayListExtra("userIds", new ArrayList<>(currentUser.getFollowing()));
                startActivity(intent);
            }
        });
    }

    private void updateFollowerCount(boolean isFollowing) {
        int currentFollowers = Integer.parseInt(followersCount.getText().toString());
        if (isFollowing) {
            followersCount.setText(String.valueOf(currentFollowers + 1));
        } else {
            followersCount.setText(String.valueOf(currentFollowers - 1));
        }
    }

    private void updateFollowButtonState() {
        if (isFollowing) {
            btnFollow.setText("Following");
            btnFollow.setBackgroundColor(Color.WHITE);
            btnFollow.setTextColor(Color.BLACK);
            btnFollow.setStrokeColor(ColorStateList.valueOf(Color.LTGRAY));
            btnFollow.setStrokeWidth(2);
        } else {
            btnFollow.setText("Follow");
            btnFollow.setBackgroundColor(ContextCompat.getColor(this, R.color.teal_700));
            btnFollow.setTextColor(Color.WHITE);
            btnFollow.setStrokeWidth(0);
        }
    }

    @Override
    public void onCommentClick(Post post) {
        Intent intent = new Intent(OtherProfileActivity.this, PostDetailActivity.class);
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
                .setPositiveButton("Delete", (dialog, which) -> profileViewModel.deletePost(post.getPostId()))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
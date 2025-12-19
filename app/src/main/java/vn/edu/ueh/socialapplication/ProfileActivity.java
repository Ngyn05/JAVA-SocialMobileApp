package vn.edu.ueh.socialapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import vn.edu.ueh.socialapplication.data.model.Post;
import vn.edu.ueh.socialapplication.data.model.User;
import vn.edu.ueh.socialapplication.follow.FollowRepository;
import vn.edu.ueh.socialapplication.offline.OfflineRepository;
import vn.edu.ueh.socialapplication.profile.MyPostAdapter;
import vn.edu.ueh.socialapplication.profile.ProfileViewModel;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView imageProfile;
    private TextView postsCount, followersCount, followingCount, fullname, bio, toolbarTitle;
    private MaterialButton btnAction;
    private ImageView optionsMenu;
    private RecyclerView recyclerViewPosts;
    private MyPostAdapter myPostAdapter;
    private List<Post> postList;
    private LinearLayout followersLayout, followingLayout;

    private ProfileViewModel viewModel;
    private FirebaseUser firebaseUser;
    private String profileId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // Get profileId from intent, or default to current user
        Intent intent = getIntent();
        profileId = intent.getStringExtra("uid");
        if (profileId == null) {
            if (firebaseUser != null) {
                profileId = firebaseUser.getUid();
            } else {
                finish(); // Should not happen if auth is handled correctly
                return;
            }
        }

        initViews();
        initViewModel();
        setupRecyclerView();
        observeViewModel();
        setupListeners();

        viewModel.loadUserProfile(profileId);
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        imageProfile = findViewById(R.id.image_profile);
        postsCount = findViewById(R.id.posts_count);
        followersCount = findViewById(R.id.followers_count);
        followingCount = findViewById(R.id.following_count);
        fullname = findViewById(R.id.fullname);
        bio = findViewById(R.id.bio);
        btnAction = findViewById(R.id.btn_action);
        optionsMenu = findViewById(R.id.options_menu);
        recyclerViewPosts = findViewById(R.id.recycler_view_posts);
        toolbarTitle = findViewById(R.id.toolbar_title);
        followersLayout = findViewById(R.id.followers_layout);
        followingLayout = findViewById(R.id.following_layout);

        // Hide options menu if not current user (or implement reporting/blocking later)
        if (!profileId.equals(firebaseUser.getUid())) {
            optionsMenu.setVisibility(View.GONE);
        }
    }

    private void initViewModel() {
        UserRepository userRepository = new UserRepository();
        FollowRepository followRepository = new FollowRepository();
        OfflineRepository offlineRepository = new OfflineRepository(this);
        ProfileViewModel.Factory factory = new ProfileViewModel.Factory(userRepository, followRepository, offlineRepository);
        viewModel = new ViewModelProvider(this, factory).get(ProfileViewModel.class);
    }

    private void setupRecyclerView() {
        recyclerViewPosts.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerViewPosts.setLayoutManager(layoutManager);
        postList = new ArrayList<>();
        myPostAdapter = new MyPostAdapter(this, postList);
        recyclerViewPosts.setAdapter(myPostAdapter);
    }

    private void observeViewModel() {
        viewModel.getUser().observe(this, user -> {
            if (user != null) {
                if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                    ImageUtils.loadImage(user.getAvatar(), imageProfile);
                }
                fullname.setText(user.getUserName());
                toolbarTitle.setText(user.getUserId()); // Handle/username in toolbar
                bio.setText(user.getBio());
            }
        });

        viewModel.getFollowersCount().observe(this, count -> followersCount.setText(String.valueOf(count)));
        viewModel.getFollowingCount().observe(this, count -> followingCount.setText(String.valueOf(count)));

        viewModel.getIsFollowing().observe(this, isFollowing -> {
            if (profileId.equals(firebaseUser.getUid())) {
                btnAction.setText("Chỉnh sửa trang cá nhân");
            } else {
                if (isFollowing) {
                    btnAction.setText("Đang theo dõi");
                    btnAction.setBackgroundColor(getResources().getColor(android.R.color.white));
                    btnAction.setTextColor(getResources().getColor(android.R.color.black));
                } else {
                    btnAction.setText("Theo dõi");
                    btnAction.setBackgroundColor(getResources().getColor(R.color.colorPrimary)); // Ensure colorPrimary is defined or use a specific color
                    btnAction.setTextColor(getResources().getColor(android.R.color.white));
                }
            }
        });

        viewModel.getUserPosts().observe(this, posts -> {
            if (posts != null) {
                postList.clear();
                postList.addAll(posts);
                myPostAdapter.notifyDataSetChanged();
                postsCount.setText(String.valueOf(posts.size()));
            }
        });

        viewModel.getError().observe(this, errorMsg -> {
            if (errorMsg != null) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        btnAction.setOnClickListener(v -> {
            String btnText = btnAction.getText().toString();
            if (btnText.equals("Chỉnh sửa trang cá nhân")) {
                startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
            } else {
                viewModel.toggleFollow(profileId);
            }
        });

        followersLayout.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, FollowListActivity.class);
            intent.putExtra("id", profileId);
            intent.putExtra("title", "followers");
            startActivity(intent);
        });

        followingLayout.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, FollowListActivity.class);
            intent.putExtra("id", profileId);
            intent.putExtra("title", "following");
            startActivity(intent);
        });
        
        optionsMenu.setOnClickListener(v -> {
            // Open Settings Activity (Member B's task, but linking it here)
            // startActivity(new Intent(ProfileActivity.this, SettingsActivity.class));
            // For now just show a toast or nothing
             Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
        });
    }
}

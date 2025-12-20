package vn.edu.ueh.socialapplication.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import vn.edu.ueh.socialapplication.R;
import vn.edu.ueh.socialapplication.data.model.Post;
import vn.edu.ueh.socialapplication.utils.ImageUtils;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView imageProfile;
    private TextView postsCount, followersCount, followingCount, fullname, bio, toolbarTitle;
    private MaterialButton btnAction;
    private ImageView optionsMenu;
    private RecyclerView recyclerViewPosts;
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

        // --- KHỞI TẠO VIEWMODEL TRƯỚC KHI SỬ DỤNG ---
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        initViews();
        observeViewModel();
        setupListeners();

        viewModel.loadUserProfile(profileId);
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        imageProfile = findViewById(R.id.image_profile);
        postsCount = findViewById(R.id.posts_count);
        followersCount = findViewById(R.id.followers_count);
        followingCount = findViewById(R.id.following_count);
        fullname = findViewById(R.id.fullname);
        bio = findViewById(R.id.bio);
        btnAction = findViewById(R.id.btn_action);
        optionsMenu = findViewById(R.id.options_menu);
//        recyclerViewPosts = findViewById(R.id.recycler_view_posts);
        toolbarTitle = findViewById(R.id.toolbar_title);
        followersLayout = findViewById(R.id.followers_layout);
        followingLayout = findViewById(R.id.following_layout);

        postList = new ArrayList<>();
        // Setup RecyclerView nếu cần thiết ở đây

        // Hide options menu if not current user
        if (firebaseUser != null && !profileId.equals(firebaseUser.getUid())) {
            optionsMenu.setVisibility(View.GONE);
        }
    }


    private void observeViewModel() {
        viewModel.getUser().observe(this, user -> {
            if (user != null) {
                if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                    ImageUtils.loadImage(user.getAvatar(), imageProfile);
                }
                fullname.setText(user.getUserName());
                toolbarTitle.setText(user.getUserId());
                bio.setText(user.getBio());
            }
        });

        viewModel.getFollowersCount().observe(this, count -> followersCount.setText(String.valueOf(count)));
        viewModel.getFollowingCount().observe(this, count -> followingCount.setText(String.valueOf(count)));

        viewModel.getIsFollowing().observe(this, isFollowing -> {
            if (firebaseUser != null && profileId.equals(firebaseUser.getUid())) {
                btnAction.setText("Chỉnh sửa trang cá nhân");
            } else {
                if (isFollowing) {
                    btnAction.setText("Đang theo dõi");
                    btnAction.setBackgroundColor(getResources().getColor(android.R.color.white));
                    btnAction.setTextColor(getResources().getColor(android.R.color.black));
                } else {
                    btnAction.setText("Theo dõi");
                    btnAction.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    btnAction.setTextColor(getResources().getColor(android.R.color.white));
                }
            }
        });

        viewModel.getUserPosts().observe(this, posts -> {
            if (posts != null) {
                postList.clear();
                postList.addAll(posts);
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

//        followersLayout.setOnClickListener(v -> {
//            // Đảm bảo FollowListActivity đã tồn tại hoặc xóa logic này nếu chưa có
//            Toast.makeText(this, "Chức năng followers", Toast.LENGTH_SHORT).show();
//        });
//
//        followingLayout.setOnClickListener(v -> {
//            Toast.makeText(this, "Chức năng following", Toast.LENGTH_SHORT).show();
//        });
        
        optionsMenu.setOnClickListener(v -> {
             Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
        });
    }
}

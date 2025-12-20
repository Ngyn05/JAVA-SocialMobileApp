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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import vn.edu.ueh.socialapplication.profile.EditProfileActivity;
import vn.edu.ueh.socialapplication.R;
import vn.edu.ueh.socialapplication.data.model.Post;
import vn.edu.ueh.socialapplication.post.PostAdapter;
import vn.edu.ueh.socialapplication.post.PostDetailActivity;
import vn.edu.ueh.socialapplication.utils.ImageUtils;

public class ProfileActivity extends AppCompatActivity implements PostAdapter.OnPostClickListener {

    private CircleImageView imageProfile;
    private TextView postsCount, followersCount, followingCount, fullname, bio, toolbarTitle;
    private MaterialButton btnAction;
    private ImageView optionsMenu;
    private RecyclerView recyclerViewPosts;
    private PostAdapter postAdapter;
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

        Intent intent = getIntent();
        profileId = intent.getStringExtra("uid");
        if (profileId == null) {
            if (firebaseUser != null) {
                profileId = firebaseUser.getUid();
            } else {
                finish();
                return;
            }
        }

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
        recyclerViewPosts = findViewById(R.id.recycler_view_posts);
        toolbarTitle = findViewById(R.id.toolbar_title);
        followersLayout = findViewById(R.id.followers_layout);
        followingLayout = findViewById(R.id.following_layout);

        // Setup RecyclerView với LinearLayoutManager (danh sách dọc)
        recyclerViewPosts.setHasFixedSize(true);
        recyclerViewPosts.setLayoutManager(new LinearLayoutManager(this));
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList, this);
        recyclerViewPosts.setAdapter(postAdapter);

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
                toolbarTitle.setText(user.getUserName());
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
                } else {
                    btnAction.setText("Theo dõi");
                }
            }
        });

        viewModel.getUserPosts().observe(this, posts -> {
            if (posts != null) {
                postAdapter.setPosts(posts);
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

        optionsMenu.setOnClickListener(v -> {
             Toast.makeText(this, "Cài đặt", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onCommentClick(Post post) {
        Intent intent = new Intent(ProfileActivity.this, PostDetailActivity.class);
        intent.putExtra("post_object", post);
        startActivity(intent);
    }
}

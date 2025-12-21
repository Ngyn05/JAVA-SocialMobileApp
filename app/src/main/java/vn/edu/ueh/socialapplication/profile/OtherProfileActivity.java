package vn.edu.ueh.socialapplication.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import vn.edu.ueh.socialapplication.R;
import vn.edu.ueh.socialapplication.data.model.Post;
import vn.edu.ueh.socialapplication.post.EditPostActivity;
import vn.edu.ueh.socialapplication.post.PostAdapter;
import vn.edu.ueh.socialapplication.post.PostDetailActivity;
import vn.edu.ueh.socialapplication.utils.ImageUtils;

public class OtherProfileActivity extends AppCompatActivity implements PostAdapter.OnPostClickListener {

    private CircleImageView imageProfile;
    private TextView postsCount, followersCount, followingCount, fullname, bio, toolbarTitle;
    private MaterialButton btnFollow;
    private ImageView backButton;
    private RecyclerView recyclerViewPosts;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private LinearLayout followersLayout, followingLayout;

    private ProfileViewModel viewModel;
    private String profileId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_profile);

        Intent intent = getIntent();
        profileId = intent.getStringExtra("USER_ID");
        if (profileId == null) {
            finish();
            return;
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
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        imageProfile = findViewById(R.id.image_profile);
        postsCount = findViewById(R.id.posts_count);
        followersCount = findViewById(R.id.followers_count);
        followingCount = findViewById(R.id.following_count);
        fullname = findViewById(R.id.fullname);
        bio = findViewById(R.id.bio);
        btnFollow = findViewById(R.id.btn_follow);
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
        
        viewModel.getDeletePostStatus().observe(this, isSuccess -> {
            if (isSuccess != null && isSuccess) {
                Toast.makeText(this, "Post deleted", Toast.LENGTH_SHORT).show();
                viewModel.loadUserProfile(profileId);
            } else if (isSuccess != null) {
                Toast.makeText(this, "Failed to delete post", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
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
            .setPositiveButton("Delete", (dialog, which) -> viewModel.deletePost(post.getPostId()))
            .setNegativeButton("Cancel", null)
            .show();
    }
}

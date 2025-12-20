package vn.edu.ueh.socialapplication.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;

import vn.edu.ueh.socialapplication.R;
import vn.edu.ueh.socialapplication.data.model.Post;
import vn.edu.ueh.socialapplication.post.CreatePostActivity;
import vn.edu.ueh.socialapplication.post.PostAdapter;
import vn.edu.ueh.socialapplication.post.PostDetailActivity;

public class HomeActivity extends AppCompatActivity implements PostAdapter.OnPostClickListener {
    private RecyclerView rvPosts;
    private PostAdapter postAdapter;
    private HomeViewModel homeViewModel;
    private ImageView btnAddPost;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar paginationProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setupViews();
        setupRecyclerView();
        setupViewModel();
        setupListeners();

        observeViewModel();
    }

    private void setupViews() {
        rvPosts = findViewById(R.id.rvPosts);
        btnAddPost = findViewById(R.id.btnAddPost);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        paginationProgressBar = findViewById(R.id.pagination_progress_bar);
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvPosts.setLayoutManager(layoutManager);
        postAdapter = new PostAdapter(new ArrayList<>(), this);
        rvPosts.setAdapter(postAdapter);
    }

    private void setupViewModel() {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
    }

    private void setupListeners() {
        btnAddPost.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CreatePostActivity.class);
            startActivity(intent);
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            homeViewModel.loadFirstPage();
        });

        rvPosts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                        homeViewModel.loadNextPage();
                    }
                }
            }
        });
    }

    private void observeViewModel() {
        homeViewModel.getPostsData().observe(this, posts -> {
            if (posts != null) {
                postAdapter.setPosts(posts);
            }
        });

        homeViewModel.getIsLoadingData().observe(this, isLoading -> {
            if (isLoading != null) {
                // Show SwipeRefresh only on initial load/refresh
                if (swipeRefreshLayout.isRefreshing() && !isLoading) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                // Show pagination progress bar only when loading next page
                boolean isFirstPage = ((LinearLayoutManager)rvPosts.getLayoutManager()).findFirstVisibleItemPosition() == 0;
                paginationProgressBar.setVisibility(isLoading && !swipeRefreshLayout.isRefreshing() ? View.VISIBLE : View.GONE);
            }
        });

        homeViewModel.getErrorData().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCommentClick(Post post) {
        if (post.getPostId() == null || post.getPostId().isEmpty()) {
            Toast.makeText(this, "Error: Post ID is missing!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(HomeActivity.this, PostDetailActivity.class);
        intent.putExtra("post_object", post);
        startActivity(intent);
    }
}

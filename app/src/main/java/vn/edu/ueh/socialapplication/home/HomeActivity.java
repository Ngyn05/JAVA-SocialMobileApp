package vn.edu.ueh.socialapplication.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import java.util.ArrayList;

import vn.edu.ueh.socialapplication.R;
import vn.edu.ueh.socialapplication.data.model.Post;
import vn.edu.ueh.socialapplication.post.PostAdapter;
import vn.edu.ueh.socialapplication.post.PostDetailActivity;

public class HomeActivity extends AppCompatActivity implements PostAdapter.OnPostClickListener {
    private RecyclerView rvPosts;
    private PostAdapter postAdapter;
    private HomeViewModel homeViewModel;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayoutManager layoutManager;

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
}

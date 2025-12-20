package vn.edu.ueh.socialapplication.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
        
        rvPosts.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo adapter
        postAdapter = new PostAdapter(new ArrayList<>(), this);
        rvPosts.setAdapter(postAdapter);

        // --- KHỞI TẠO VIEWMODEL ---
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // --- LẮNG NGHE DỮ LIỆU ---
        observeViewModel();

        // --- SETUP SWIPE TO REFRESH ---
        swipeRefreshLayout.setOnRefreshListener(() -> {
            homeViewModel.loadFollowingFeed();
        });

        // --- TẢI DỮ LIỆU BAN ĐẦU (FOLLOWING FEED) ---
        swipeRefreshLayout.setRefreshing(true);
        homeViewModel.loadFollowingFeed();
    }

    private void observeViewModel() {
        homeViewModel.getPostsData().observe(this, posts -> {
            swipeRefreshLayout.setRefreshing(false);
            if (posts != null) {
                postAdapter.setPosts(posts);
                if (posts.isEmpty()) {
                    Toast.makeText(this, "Hãy follow thêm người để thấy bài viết!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Không thể tải danh sách bài đăng", Toast.LENGTH_SHORT).show();
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

package vn.edu.ueh.socialapplication.home;

import android.content.Intent;
import android.os.Bundle;import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider; // Thêm import này
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import vn.edu.ueh.socialapplication.R;
import vn.edu.ueh.socialapplication.data.model.Post;
import vn.edu.ueh.socialapplication.post.PostAdapter;
import vn.edu.ueh.socialapplication.post.PostDetailActivity; // Sửa lại import cho gọn

public class HomeActivity extends AppCompatActivity implements PostAdapter.OnPostClickListener {
    private RecyclerView rvPosts;
    private PostAdapter postAdapter;
    private HomeViewModel homeViewModel; // << 1. KHAI BÁO VIEWMODEL

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

        // --- ÁNH XẠ VIEW VÀ SETUP RECYCLERVIEW ---
        rvPosts = findViewById(R.id.rvPosts);
        rvPosts.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo adapter với một danh sách rỗng ban đầu
        postAdapter = new PostAdapter(new ArrayList<>(), this);
        rvPosts.setAdapter(postAdapter);

        // --- 2. KHỞI TẠO VIEWMODEL ---
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // --- 3. LẮNG NGHE DỮ LIỆU TỪ VIEWMODEL ---
        // Xóa bỏ hoàn toàn phần tạo dữ liệu giả
        observeViewModel();

        // --- 4. YÊU CẦU VIEWMODEL TẢI DỮ LIỆU ---
        homeViewModel.loadFeed();
    }

    private void observeViewModel() {
        homeViewModel.getPostsData().observe(this, posts -> {
            // Khi ViewModel có dữ liệu mới, nó sẽ tự động gọi vào đây
            if (posts != null) {
                // Cập nhật dữ liệu cho Adapter để hiển thị lên RecyclerView
                postAdapter.setPosts(posts);
                // Hoặc bạn có thể thêm logic hiển thị/ẩn ProgressBar ở đây
            } else {
                // Xử lý trường hợp có lỗi xảy ra khi tải dữ liệu
                Toast.makeText(this, "Không thể tải danh sách bài đăng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCommentClick(Post post) {
        // Chuyển sang Activity chi tiết bài đăng
        Intent intent = new Intent(HomeActivity.this, PostDetailActivity.class);

        // Đóng gói đối tượng Post và gửi đi
        intent.putExtra("post_object", post);

        startActivity(intent);
    }
}

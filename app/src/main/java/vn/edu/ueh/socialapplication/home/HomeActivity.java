package vn.edu.ueh.socialapplication.home;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

//import com.cloudinary.android.MediaManager;


import java.util.ArrayList;
import java.util.List;

import vn.edu.ueh.socialapplication.R;
import vn.edu.ueh.socialapplication.data.model.Post;
import vn.edu.ueh.socialapplication.post.PostAdapter;

public class HomeActivity extends AppCompatActivity implements PostAdapter.OnPostClickListener {
    private RecyclerView rvPosts;
    private PostAdapter postAdapter;
    private List<Post> postList;

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

        rvPosts = findViewById(R.id.rvPosts);
        rvPosts.setLayoutManager(new LinearLayoutManager(this));

        // --- 3. TẠO DỮ LIỆU GIẢ ---
        postList = new ArrayList<>();

        // Thêm vài bài post test (Lấy link ảnh trên mạng test tạm)
        postList.add(new Post("Nguyễn Văn A", "Hôm nay trời đẹp quá!", "https://plus.unsplash.com/premium_photo-1664474619075-644dd191935f"));
        postList.add(new Post("Trần Thị B", "Vừa học code Android xong, mệt nhưng vui.", "https://images.unsplash.com/photo-1498050108023-c5249f4df085"));
        postList.add(new Post("Lê Văn C", "Test Cloudinary nào anh em.", "")); // Bài này không có ảnh

        // --- 4. GẮN ADAPTER ---
        postAdapter = new PostAdapter(postList, this);
        rvPosts.setAdapter(postAdapter);
    }

    @Override
    public void onCommentClick(Post post) {
        // Không show dialog nữa, mà chuyển sang Activity mới
        Intent intent = new Intent(HomeActivity.this, vn.edu.ueh.socialapplication.post.PostDetailActivity.class);

        // Đóng gói bài post gửi sang bên kia
        intent.putExtra("post_object", post);

        startActivity(intent);
    }
}

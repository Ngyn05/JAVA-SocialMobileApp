package vn.edu.ueh.socialapplication.post; // Chú ý package

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import vn.edu.ueh.socialapplication.R;
import vn.edu.ueh.socialapplication.data.model.Comment;
import vn.edu.ueh.socialapplication.data.model.Post;
import vn.edu.ueh.socialapplication.post.CommentAdapter;

public class PostDetailActivity extends AppCompatActivity {

    private Post currentPost;
    private CommentAdapter commentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // 1. Nhận dữ liệu Post từ HomeActivity gửi sang
        if (getIntent().getExtras() != null) {
            currentPost = (Post) getIntent().getSerializableExtra("post_object");
        }

        // 2. Ánh xạ View
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView tvUser = findViewById(R.id.tvDetailUserName);
        TextView tvContent = findViewById(R.id.tvDetailContent);
        ImageView imgPost = findViewById(R.id.imgDetailPost);
        RecyclerView rvComments = findViewById(R.id.rvDetailComments);
        EditText edtInput = findViewById(R.id.edtDetailCommentInput);
        ImageView btnSend = findViewById(R.id.btnDetailSend);

        // 3. Hiển thị dữ liệu bài viết
        if (currentPost != null) {
            tvUser.setText(currentPost.getUserName());
            tvContent.setText(currentPost.getContent());

            if (currentPost.getImageUrl() != null && !currentPost.getImageUrl().isEmpty()) {
                imgPost.setVisibility(View.VISIBLE);
                Glide.with(this).load(currentPost.getImageUrl()).into(imgPost);
            }

            // 4. Setup danh sách Comment
            rvComments.setLayoutManager(new LinearLayoutManager(this));
            List<Comment> listComments = currentPost.getCommentList();
            commentAdapter = new CommentAdapter(listComments);
            rvComments.setAdapter(commentAdapter);

            // 5. Xử lý nút Gửi Comment
            btnSend.setOnClickListener(v -> {
                String content = edtInput.getText().toString().trim();
                if (!content.isEmpty()) {
                    Comment newCmt = new Comment("Tôi", content);

                    // Thêm vào list và cập nhật giao diện
                    listComments.add(newCmt);
                    commentAdapter.notifyItemInserted(listComments.size() - 1);

                    // Xóa ô nhập
                    edtInput.setText("");
                }
            });
        }

        // 6. Xử lý nút Back
        btnBack.setOnClickListener(v -> finish());
    }
}
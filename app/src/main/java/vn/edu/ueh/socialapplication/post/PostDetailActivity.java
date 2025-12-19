package vn.edu.ueh.socialapplication.post;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast; // Thêm Toast để thông báo
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider; // Thêm ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList; // Dùng ArrayList để khởi tạo
import java.util.List;
import vn.edu.ueh.socialapplication.R;
import vn.edu.ueh.socialapplication.data.model.Comment;
import vn.edu.ueh.socialapplication.data.model.Post;

public class PostDetailActivity extends AppCompatActivity {

    private Post currentPost;
    private CommentAdapter commentAdapter;
    private PostDetailViewModel viewModel; // << THÊM VIEWMODEL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(PostDetailViewModel.class);

        // 1. Nhận dữ liệu Post
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
        TextView tvLikeCount = findViewById(R.id.tvLikeCount);
        TextView tvCommentCount = findViewById(R.id.tvCommentCount);

        // 3. Hiển thị dữ liệu bài viết
        if (currentPost != null) {
            tvUser.setText(currentPost.getUserName());
            tvContent.setText(currentPost.getContent());

            if (currentPost.getImage() != null && !currentPost.getImage().isEmpty()) {
                imgPost.setVisibility(View.VISIBLE);
                Glide.with(this).load(currentPost.getImage()).into(imgPost);
            }

            if (tvLikeCount != null) {
                tvLikeCount.setText(String.valueOf(currentPost.getLikesCount()));
            }

            // 4. LOAD COMMENT COUNT (Lấy từ field int comments)
            if (tvCommentCount != null) {
                tvCommentCount.setText(String.valueOf(currentPost.getComments()));
            }

            // 4. Setup danh sách Comment
            rvComments.setLayoutManager(new LinearLayoutManager(this));
            // Khởi tạo Adapter với danh sách rỗng trước
            commentAdapter = new CommentAdapter(new ArrayList<>());
            rvComments.setAdapter(commentAdapter);

            // << THAY ĐỔI: Lắng nghe dữ liệu bình luận từ ViewModel >>
            viewModel.getCommentsData().observe(this, comments -> {
                if (comments != null) {
                    commentAdapter.setCommentList(comments); // Cập nhật adapter
                }
            });

            // Tải danh sách bình luận lần đầu
            viewModel.loadComments(currentPost.getPostId());

            // 5. Xử lý nút Gửi Comment
            btnSend.setOnClickListener(v -> {
                String content = edtInput.getText().toString().trim();
                if (!content.isEmpty()) {
                    btnSend.setEnabled(false); // Vô hiệu hóa nút gửi để tránh spam
                    viewModel.postComment(currentPost.getPostId(), content);
                }
            });

            // << THÊM: Lắng nghe kết quả gửi comment >>
            viewModel.getCommentPostStatus().observe(this, success -> {
                btnSend.setEnabled(true); // Bật lại nút gửi
                if (success) {
                    edtInput.setText(""); // Xóa ô nhập liệu khi thành công
                    rvComments.scrollToPosition(commentAdapter.getItemCount() - 1); // Cuộn xuống bình luận mới nhất
                } else {
                    Toast.makeText(this, "Gửi bình luận thất bại!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // 6. Xử lý nút Back
        btnBack.setOnClickListener(v -> finish());


    }

}
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

        View postCardView = findViewById(R.id.postCard);
        TextView tvUser = postCardView.findViewById(R.id.tvUsername);
        TextView tvContent = findViewById(R.id.tvCaption);
        ImageView imgPost = findViewById(R.id.imgPost);
        ImageView btnLike = postCardView.findViewById(R.id.btnLike);
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

            // Cập nhật trạng thái Like ban đầu (Đổi màu trái tim)
            updateLikeUI(btnLike, tvLikeCount, currentPost);

            // Xử lý sự kiện click Like
            btnLike.setOnClickListener(v -> {
                // 1. Cập nhật local UI ngay lập tức
                toggleLocalLike(btnLike, tvLikeCount, currentPost);

                // 2. Gọi ViewModel để cập nhật lên Firestore
                // Giả sử ViewModel của bạn đã có hàm toggleLike tương tự Adapter
                viewModel.toggleLike(currentPost.getPostId());
            });

            updateLikeCommentCount(tvCommentCount, tvLikeCount, currentPost);

            // 4. Setup danh sách Comment
            rvComments.setLayoutManager(new LinearLayoutManager(this));
            // Khởi tạo Adapter với danh sách rỗng trước
            commentAdapter = new CommentAdapter();
            rvComments.setAdapter(commentAdapter);

            viewModel.listenForComments(currentPost.getPostId());
            viewModel.getCommentsData().observe(this, comments -> {
                commentAdapter.setComments(comments);
            });

            btnSend.setOnClickListener(v -> {
                String content = edtInput.getText().toString().trim();
                if (!content.isEmpty()) {
                    // Gọi hàm gửi
                    viewModel.sendComment(currentPost.getPostId(), content);

                    // ẨN BÀN PHÍM SAU KHI BẤM GỬI
                    hideKeyboard();
                }
            });


            viewModel.getCommentPostStatus().observe(this, isSuccess -> {
                if (isSuccess != null && isSuccess) {
                    edtInput.setText("");
                    Toast.makeText(this, "Đã gửi bình luận", Toast.LENGTH_SHORT).show();
                } else if (isSuccess != null && !isSuccess) {
                    Toast.makeText(this, "Gửi bình luận thất bại", Toast.LENGTH_SHORT).show();
                }
            });

            viewModel.listenForPostChanges(currentPost.getPostId());

            viewModel.getPostData().observe(this, updatedPost -> {
                if (updatedPost != null) {
                    updateLikeCommentCount(tvCommentCount, tvLikeCount, updatedPost);
                    this.currentPost = updatedPost;
                }
            });
        }

        // 6. Xử lý nút Back
        btnBack.setOnClickListener(v -> finish());


    }

    private void updateLikeUI(ImageView btnLike, TextView tvLikeCount, Post post) {
        String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (currentUserId != null && post.getLikes() != null && post.getLikes().contains(currentUserId)) {
            // TRƯỜNG HỢP ĐÃ LIKE
            btnLike.setImageResource(R.drawable.ic_liked); // Dùng icon trái tim đầy
        } else {
            // TRƯỜNG HỢP CHƯA LIKE
            btnLike.setImageResource(R.drawable.ic_unliked); // Dùng icon trái tim trống
        }
        tvLikeCount.setText(String.valueOf(post.getLikesCount()));
    }

    // Hàm xử lý click nhanh (Optimistic Update)
    private void toggleLocalLike(ImageView btnLike, TextView tvLikeCount, Post post) {
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        List<String> likes = post.getLikes();

        if (likes.contains(uid)) {
            likes.remove(uid);
        } else {
            likes.add(uid);
        }

        post.setLikes(likes);
        updateLikeUI(btnLike, tvLikeCount, post);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private void updateLikeCommentCount(TextView tvCommentCount, TextView tvLikeCount, Post updatedPost) {
        tvCommentCount.setText(String.valueOf(updatedPost.getComments()));
        tvLikeCount.setText(String.valueOf(updatedPost.getLikesCount()));
    }
}
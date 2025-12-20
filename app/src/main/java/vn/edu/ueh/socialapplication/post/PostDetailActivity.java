package vn.edu.ueh.socialapplication.post;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import vn.edu.ueh.socialapplication.R;
import vn.edu.ueh.socialapplication.data.model.Post;

public class PostDetailActivity extends AppCompatActivity {

    private Post currentPost;
    private CommentAdapter commentAdapter;
    private PostDetailViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        viewModel = new ViewModelProvider(this).get(PostDetailViewModel.class);

        if (getIntent().getExtras() != null) {
            currentPost = (Post) getIntent().getSerializableExtra("post_object");
        }

        ImageView btnBack = findViewById(R.id.btnBack);
        View postCardView = findViewById(R.id.postCard);

        // Ánh xạ các View bên trong include (postCard)
        TextView tvUser = postCardView.findViewById(R.id.tvUsername);
        TextView tvContent = postCardView.findViewById(R.id.tvCaption);
        TextView tvDate = postCardView.findViewById(R.id.tvDate);
        ImageView imgPost = postCardView.findViewById(R.id.imgPost);
        CardView cardImage = postCardView.findViewById(R.id.cardImage);
        ImageView btnLike = postCardView.findViewById(R.id.btnLike);
        TextView tvLikeCount = postCardView.findViewById(R.id.tvLikeCount);
        TextView tvCommentCount = postCardView.findViewById(R.id.tvCommentCount);

        RecyclerView rvComments = findViewById(R.id.rvDetailComments);
        EditText edtInput = findViewById(R.id.edtDetailCommentInput);
        ImageView btnSend = findViewById(R.id.btnDetailSend);

        if (currentPost != null) {
            tvUser.setText(currentPost.getUserName());
            tvContent.setText(currentPost.getContent());
            
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
            if (currentPost.getCreatedAt() != null) {
                tvDate.setText(sdf.format(currentPost.getCreatedAt()));
            }

            // XỬ LÝ ẨN/HIỆN HÌNH ẢNH (Quan trọng)
            if (currentPost.getImage() != null && !currentPost.getImage().isEmpty()) {
                cardImage.setVisibility(View.VISIBLE);
                Glide.with(this).load(currentPost.getImage()).into(imgPost);
            } else {
                cardImage.setVisibility(View.GONE);
            }

            updateLikeUI(btnLike, tvLikeCount, currentPost);

            btnLike.setOnClickListener(v -> {
                toggleLocalLike(btnLike, tvLikeCount, currentPost);
                viewModel.toggleLike(currentPost.getPostId());
            });

            updateLikeCommentCount(tvCommentCount, tvLikeCount, currentPost);

            rvComments.setLayoutManager(new LinearLayoutManager(this));
            commentAdapter = new CommentAdapter();
            rvComments.setAdapter(commentAdapter);

            viewModel.listenForComments(currentPost.getPostId());
            viewModel.getCommentsData().observe(this, comments -> {
                commentAdapter.setComments(comments);
            });

            btnSend.setOnClickListener(v -> {
                String content = edtInput.getText().toString().trim();
                if (!content.isEmpty()) {
                    viewModel.sendComment(currentPost.getPostId(), content);
                    hideKeyboard();
                }
            });

            viewModel.getCommentPostStatus().observe(this, isSuccess -> {
                if (isSuccess != null && isSuccess) {
                    edtInput.setText("");
                    Toast.makeText(this, "Đã gửi bình luận", Toast.LENGTH_SHORT).show();
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

        btnBack.setOnClickListener(v -> finish());
    }

    private void updateLikeUI(ImageView btnLike, TextView tvLikeCount, Post post) {
        String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (currentUserId != null && post.getLikes() != null && post.getLikes().contains(currentUserId)) {
            btnLike.setImageResource(R.drawable.ic_liked);
        } else {
            btnLike.setImageResource(R.drawable.ic_unliked);
        }
        tvLikeCount.setText(String.valueOf(post.getLikesCount()));
    }

    private void toggleLocalLike(ImageView btnLike, TextView tvLikeCount, Post post) {
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        List<String> likes = post.getLikes();
        if (likes.contains(uid)) likes.remove(uid);
        else likes.add(uid);
        post.setLikes(likes);
        updateLikeUI(btnLike, tvLikeCount, post);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void updateLikeCommentCount(TextView tvCommentCount, TextView tvLikeCount, Post updatedPost) {
        tvCommentCount.setText(String.valueOf(updatedPost.getComments()));
        tvLikeCount.setText(String.valueOf(updatedPost.getLikesCount()));
    }
}

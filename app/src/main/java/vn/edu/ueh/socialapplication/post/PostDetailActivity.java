package vn.edu.ueh.socialapplication.post;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import vn.edu.ueh.socialapplication.R;
import vn.edu.ueh.socialapplication.data.model.Post;

public class PostDetailActivity extends AppCompatActivity {

    private static final int EDIT_POST_REQUEST = 1;
    private Post currentPost;
    private CommentAdapter commentAdapter;
    private PostDetailViewModel viewModel;
    private TextView tvCaption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        viewModel = new ViewModelProvider(this).get(PostDetailViewModel.class);

        if (getIntent().getExtras() != null) {
            currentPost = (Post) getIntent().getSerializableExtra("post_object");
        }

        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView btnMore = findViewById(R.id.btnMore);

        TextView tvUsername = findViewById(R.id.tvUsername);
        TextView tvTimestamp = findViewById(R.id.tvTimestamp);
        tvCaption = findViewById(R.id.tvCaption);

        ImageView imgPost = findViewById(R.id.imgPost);
        ImageView imgPost2 = findViewById(R.id.imgPost2);
        CardView cardImage = findViewById(R.id.cardImage);
        ImageView btnLike = findViewById(R.id.btnLike);
        TextView tvLikeCount = findViewById(R.id.tvLikeCount);
        TextView tvCommentCount = findViewById(R.id.tvCommentCount);

        RecyclerView rvComments = findViewById(R.id.rvDetailComments);
        EditText edtInput = findViewById(R.id.edtDetailCommentInput);
        ImageView btnSend = findViewById(R.id.btnDetailSend);

        if (currentPost != null) {
            tvUsername.setText(currentPost.getUserName());
            tvCaption.setText(currentPost.getContent());

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            if (currentPost.getCreatedAt() != null) {
                tvTimestamp.setText(sdf.format(currentPost.getCreatedAt()));
            }

            if (currentPost.getImage() != null && !currentPost.getImage().isEmpty()) {
                cardImage.setVisibility(View.VISIBLE);
                Glide.with(this).load(currentPost.getImage()).into(imgPost);
                imgPost2.setVisibility(View.GONE);
            } else {
                cardImage.setVisibility(View.GONE);
            }
            
            String currentUserId = FirebaseAuth.getInstance().getUid();
            if (currentUserId != null && currentUserId.equals(currentPost.getUserId())) {
                btnMore.setVisibility(View.VISIBLE);
            } else {
                btnMore.setVisibility(View.GONE);
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
            viewModel.getCommentsData().observe(this, comments -> commentAdapter.setComments(comments));

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

            viewModel.getDeletePostStatus().observe(this, isSuccess -> {
                if (isSuccess != null && isSuccess) {
                    Toast.makeText(this, "Post deleted", Toast.LENGTH_SHORT).show();
                    finish();
                } else if (isSuccess != null) {
                    Toast.makeText(this, "Failed to delete post", Toast.LENGTH_SHORT).show();
                }
            });
            
            btnMore.setOnClickListener(this::showPopupMenu);
        }

        btnBack.setOnClickListener(v -> finish());
    }

    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.post_options_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_edit) {
                editPost();
                return true;
            } else if (itemId == R.id.menu_delete) {
                confirmDeletePost();
                return true;
            }
            return false;
        });
        popup.show();
    }
    
    private void editPost() {
        Intent intent = new Intent(this, EditPostActivity.class);
        intent.putExtra("post_id", currentPost.getPostId());
        intent.putExtra("post_content", currentPost.getContent());
        startActivityForResult(intent, EDIT_POST_REQUEST);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_POST_REQUEST && resultCode == RESULT_OK && data != null) {
            String updatedContent = data.getStringExtra("updated_content");
            if (updatedContent != null) {
                tvCaption.setText(updatedContent);
                currentPost.setContent(updatedContent);
            }
        }
    }

    private void confirmDeletePost() {
        new AlertDialog.Builder(this)
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this post?")
            .setPositiveButton("Delete", (dialog, which) -> viewModel.deletePost(currentPost.getPostId()))
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void updateLikeUI(ImageView btnLike, TextView tvLikeCount, Post post) {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId != null && post.getLikes() != null && post.getLikes().contains(currentUserId)) {
            btnLike.setImageResource(R.drawable.ic_liked);
        } else {
            btnLike.setImageResource(R.drawable.ic_unliked);
        }
        tvLikeCount.setText(String.valueOf(post.getLikesCount()));
    }

    private void toggleLocalLike(ImageView btnLike, TextView tvLikeCount, Post post) {
        String uid = FirebaseAuth.getInstance().getUid();
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

package vn.edu.ueh.socialapplication.post;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;
import vn.edu.ueh.socialapplication.R;
import vn.edu.ueh.socialapplication.data.model.User;

public class EditPostActivity extends AppCompatActivity {

    private EditText postContentEditText;
    private ImageView postImageView;
    private ProgressBar progressBar;
    private TextView btnSave;
    private CircleImageView userProfileImageView;
    private TextView userNameTextView;
    private String postId;
    private PostViewModel postViewModel;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        PostViewModelFactory factory = new PostViewModelFactory(getApplication());
        postViewModel = new ViewModelProvider(this, factory).get(PostViewModel.class);

        postContentEditText = findViewById(R.id.post_content_edit_text);
        postImageView = findViewById(R.id.post_image_view);
        progressBar = findViewById(R.id.progress_bar);
        ImageView btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnSave);
        userProfileImageView = findViewById(R.id.user_profile_image);
        userNameTextView = findViewById(R.id.user_name);

        Intent intent = getIntent();
        postId = intent.getStringExtra("post_id");
        String currentContent = intent.getStringExtra("post_content");
        String postImageUrl = intent.getStringExtra("post_image_url");

        if (currentContent != null) {
            postContentEditText.setText(currentContent);
        }

        if (postImageUrl != null && !postImageUrl.isEmpty()) {
            postImageView.setVisibility(View.VISIBLE);
            Glide.with(this).load(postImageUrl).into(postImageView);
        }

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> savePost());

        loadUserProfile();
        observeViewModel();
    }

    private void loadUserProfile() {
        if (currentUser != null) {
            DocumentReference userRef = db.collection("users").document(currentUser.getUid());
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        userNameTextView.setText(user.getUserName());
                        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                            Glide.with(this).load(user.getAvatar()).into(userProfileImageView);
                        }
                    }
                }
            });
        }
    }

    private void savePost() {
        String newContent = postContentEditText.getText().toString().trim();

        if (newContent.isEmpty()) {
            Toast.makeText(this, "Content cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (postId != null) {
            setLoading(true);
            postViewModel.updatePost(postId, newContent);
        }
    }

    private void observeViewModel() {
        postViewModel.getPostUpdateResult().observe(this, success -> {
            if (success) {
                setLoading(false);
                Toast.makeText(this, "Post updated successfully", Toast.LENGTH_SHORT).show();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("updated_content", postContentEditText.getText().toString().trim());
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });

        postViewModel.getPostUpdateError().observe(this, error -> {
            if (error != null) {
                setLoading(false);
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!isLoading);
    }
}
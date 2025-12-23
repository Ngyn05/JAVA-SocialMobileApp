package vn.edu.ueh.socialapplication.post;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

public class CreatePostActivity extends AppCompatActivity {

    private ImageView postImageView;
    private EditText postContentEditText;
    private TextView btnPost;
    private ImageView btnBack;
    private ProgressBar progressBar;
    private Button addPhotoButton;
    private CircleImageView userProfileImageView;
    private TextView userNameTextView;

    private Uri imageUri;
    private PostViewModel postViewModel;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    Glide.with(this).load(imageUri).into(postImageView);
                    postImageView.setVisibility(View.VISIBLE);
                    addPhotoButton.setVisibility(View.GONE);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        PostViewModelFactory factory = new PostViewModelFactory(getApplication());
        postViewModel = new ViewModelProvider(this, factory).get(PostViewModel.class);

        postImageView = findViewById(R.id.post_image_view);
        postContentEditText = findViewById(R.id.post_content_edit_text);
        btnPost = findViewById(R.id.btnPost);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progress_bar);
        addPhotoButton = findViewById(R.id.add_photo_button);
        userProfileImageView = findViewById(R.id.user_profile_image);
        userNameTextView = findViewById(R.id.user_name);

        addPhotoButton.setOnClickListener(v -> openImagePicker());
        postImageView.setOnClickListener(v -> openImagePicker());

        btnPost.setOnClickListener(v -> createPost());

        btnBack.setOnClickListener(v -> finish());

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

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void createPost() {
        String content = postContentEditText.getText().toString().trim();
        if (content.isEmpty() && imageUri == null) {
            Toast.makeText(this, "Please add content or an image", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        postViewModel.createPost(content, imageUri);
    }

    private void observeViewModel() {
        postViewModel.getPostCreationResult().observe(this, success -> {
            if (success) {
                setLoading(false);
                Toast.makeText(this, "Post created successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        postViewModel.getPostCreationError().observe(this, error -> {
            if (error != null) {
                setLoading(false);
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnPost.setEnabled(!isLoading);
    }
}
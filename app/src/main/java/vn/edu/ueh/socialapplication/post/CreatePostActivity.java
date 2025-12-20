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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

import vn.edu.ueh.socialapplication.R;

public class CreatePostActivity extends AppCompatActivity {

    private ImageView postImageView;
    private EditText postContentEditText;
    private Button postButton;
    private ProgressBar progressBar;

    private Uri imageUri;
    private PostViewModel postViewModel;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    Glide.with(this).load(imageUri).into(postImageView);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        // Create the factory
        PostViewModelFactory factory = new PostViewModelFactory(getApplication());
        // Use the factory to get the ViewModel
        postViewModel = new ViewModelProvider(this, factory).get(PostViewModel.class);

        postImageView = findViewById(R.id.post_image_view);
        postContentEditText = findViewById(R.id.post_content_edit_text);
        postButton = findViewById(R.id.post_button);
        progressBar = findViewById(R.id.progress_bar);

        postImageView.setOnClickListener(v -> openImagePicker());

        postButton.setOnClickListener(v -> createPost());

        observeViewModel();
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
        postButton.setEnabled(!isLoading);
    }
}

package vn.edu.ueh.socialapplication.post;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

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

import com.bumptech.glide.Glide;

import vn.edu.ueh.socialapplication.R;

public class CreatePostActivity extends AppCompatActivity {

    private ImageView postImage;
    private EditText postText;
    private Button submitPostButton;
    private ProgressBar progressBar;
    private Uri imageUri;
    private PostViewModel postViewModel;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    Glide.with(this).load(imageUri).into(postImage);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);

        postImage = findViewById(R.id.post_image);
        postText = findViewById(R.id.post_text);
        submitPostButton = findViewById(R.id.submit_post_button);
        progressBar = findViewById(R.id.progress_bar);

        postImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        submitPostButton.setOnClickListener(v -> {
            String content = postText.getText().toString().trim();
            if (!content.isEmpty() || imageUri != null) {
                setLoading(true);
                postViewModel.createPost(content, imageUri);
            } else {
                Toast.makeText(this, "Please add content or an image.", Toast.LENGTH_SHORT).show();
            }
        });

        observeViewModel();
    }

    private void observeViewModel() {
        postViewModel.getPostCreationResult().observe(this, success -> {
            if (success) {
                setLoading(false);
                Toast.makeText(this, "Post created successfully!", Toast.LENGTH_SHORT).show();
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
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            submitPostButton.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            submitPostButton.setEnabled(true);
        }
    }
}

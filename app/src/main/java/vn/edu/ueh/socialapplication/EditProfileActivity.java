package vn.edu.ueh.socialapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";

    private CircleImageView profileImageView;
    private TextView changePhotoText, userIdTextEdit;
    private EditText userNameEdit, bioEdit;
    private EditText currentPasswordEdit, newPasswordEdit, confirmPasswordEdit;
    private Button saveInfoButton, changePasswordButton;

    private UserRepository userRepository;
    private FirebaseUser firebaseUser;
    private Uri imageUri;
    private ProgressDialog progressDialog;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    imageUri = result.getData().getData();
                    profileImageView.setImageURI(imageUri);
                    uploadImageToCloudinary();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize views
        profileImageView = findViewById(R.id.profile_image_edit);
        changePhotoText = findViewById(R.id.change_photo_text);
        userIdTextEdit = findViewById(R.id.userId_text_edit);
        userNameEdit = findViewById(R.id.userName_edit);
        bioEdit = findViewById(R.id.bio_edit);
        currentPasswordEdit = findViewById(R.id.current_password_edit);
        newPasswordEdit = findViewById(R.id.new_password_edit);
        confirmPasswordEdit = findViewById(R.id.confirm_password_edit);
        saveInfoButton = findViewById(R.id.save_info_button);
        changePasswordButton = findViewById(R.id.change_password_button);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");

        userRepository = new UserRepository();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            loadUserInfo();
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set listeners
        saveInfoButton.setOnClickListener(v -> saveProfileInfo());
        changePasswordButton.setOnClickListener(v -> changePassword());
        profileImageView.setOnClickListener(v -> openImageChooser());
        changePhotoText.setOnClickListener(v -> openImageChooser());
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imagePickerLauncher.launch(intent);
    }

    private void uploadImageToCloudinary() {
        if (imageUri != null) {
            progressDialog.show();
            String requestId = MediaManager.get().upload(imageUri).callback(new UploadCallback() {
                @Override
                public void onStart(String requestId) {
                    Log.d(TAG, "Cloudinary upload started.");
                }

                @Override
                public void onProgress(String requestId, long bytes, long totalBytes) {}

                @Override
                public void onSuccess(String requestId, Map resultData) {
                    progressDialog.dismiss();
                    String imageUrl = (String) resultData.get("secure_url");
                    Log.d(TAG, "Cloudinary upload success. URL: " + imageUrl);
                    updateAvatarInFirestore(imageUrl);
                }

                @Override
                public void onError(String requestId, ErrorInfo error) {
                    progressDialog.dismiss();
                    Log.e(TAG, "Cloudinary upload error: " + error.getDescription());
                    Toast.makeText(EditProfileActivity.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onReschedule(String requestId, ErrorInfo error) {}
            }).dispatch();
        }
    }

    private void updateAvatarInFirestore(String imageUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("avatar", imageUrl);

        userRepository.updateUser(firebaseUser.getUid(), updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(EditProfileActivity.this, "Avatar updated!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, "Failed to update avatar URL.", Toast.LENGTH_SHORT).show());
    }

    private void loadUserInfo() {
        userRepository.getUser(firebaseUser.getUid(), new UserRepository.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                if (user != null) {
                    userIdTextEdit.setText(user.getUserId());
                    userNameEdit.setText(user.getUserName());
                    bioEdit.setText(user.getBio());
                    if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                        ImageUtils.loadImage(user.getAvatar(), profileImageView);
                    }
                }
            }
            @Override
            public void onError(Exception e) {
                Toast.makeText(EditProfileActivity.this, "Failed to load user info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfileInfo() {
        String userName = userNameEdit.getText().toString().trim();
        String bio = bioEdit.getText().toString().trim();

        Map<String, Object> updates = new HashMap<>();
        updates.put("userName", userName);
        updates.put("bio", bio);

        userRepository.updateUser(firebaseUser.getUid(), updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(EditProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void changePassword() {
       // ... (change password logic remains the same)
    }
}

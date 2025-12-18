package vn.edu.ueh.socialapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private CircleImageView profileImageView;
    private TextView changePhotoText, userIdTextEdit;
    private EditText userNameEdit, bioEdit;
    private EditText currentPasswordEdit, newPasswordEdit, confirmPasswordEdit;
    private Button saveInfoButton, changePasswordButton;

    private UserRepository userRepository;
    private FirebaseUser firebaseUser;
    private StorageReference storageReference;

    private Uri imageUri;

    // Modern way to handle activity results
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    imageUri = result.getData().getData();
                    profileImageView.setImageURI(imageUri);
                    uploadImage();
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

        userRepository = new UserRepository();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("avatars");

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

    private void uploadImage() {
        if (imageUri != null) {
            final StorageReference fileReference = storageReference.child(firebaseUser.getUid() + ".jpg");

            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        updateAvatarInFirestore(imageUrl);
                    }))
                    .addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
        String currentPassword = currentPasswordEdit.getText().toString();
        String newPassword = newPasswordEdit.getText().toString();
        String confirmPassword = confirmPasswordEdit.getText().toString();

        if (TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "All password fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "New password must be at least 6 characters.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "New passwords do not match.", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), currentPassword);

        firebaseUser.reauthenticate(credential).addOnCompleteListener(reauthTask -> {
            if (reauthTask.isSuccessful()) {
                firebaseUser.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        Toast.makeText(EditProfileActivity.this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                        currentPasswordEdit.setText("");
                        newPasswordEdit.setText("");
                        confirmPasswordEdit.setText("");
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Failed to update password: " + updateTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(EditProfileActivity.this, "Re-authentication failed: " + reauthTask.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}

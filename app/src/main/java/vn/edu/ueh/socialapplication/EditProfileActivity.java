package vn.edu.ueh.socialapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";

    private ImageView backButton, cameraIcon;
    private CircleImageView profileImageView;
    private TextView emailText, userIdText;
    private EditText userNameEdit, bioEdit;
    private TextInputEditText currentPasswordEdit, newPasswordEdit, confirmPasswordEdit;
    private MaterialButton updateButton, changePasswordButton;

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

        // Initialize all views from the new layout
        backButton = findViewById(R.id.back_button_edit);
        cameraIcon = findViewById(R.id.camera_icon);
        profileImageView = findViewById(R.id.profile_image_edit);
        emailText = findViewById(R.id.email_text);
        userIdText = findViewById(R.id.userId_text);
        userNameEdit = findViewById(R.id.userName_edit);
        bioEdit = findViewById(R.id.bio_edit);
        updateButton = findViewById(R.id.update_button);
        currentPasswordEdit = findViewById(R.id.current_password_edit);
        newPasswordEdit = findViewById(R.id.new_password_edit);
        confirmPasswordEdit = findViewById(R.id.confirm_password_edit);
        changePasswordButton = findViewById(R.id.change_password_button);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang tải lên...");

        userRepository = new UserRepository();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            loadUserInfo();
        } else {
            Toast.makeText(this, "Người dùng chưa được xác thực", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set listeners
        backButton.setOnClickListener(v -> finish());
        updateButton.setOnClickListener(v -> updateProfileInfo());
        changePasswordButton.setOnClickListener(v -> changePassword());
        profileImageView.setOnClickListener(v -> openImageChooser());
        cameraIcon.setOnClickListener(v -> openImageChooser());
    }

    private void loadUserInfo() {
        userRepository.getUser(firebaseUser.getUid(), new UserRepository.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                if (user != null) {
                    emailText.setText(user.getEmail());
                    userIdText.setText("@" + user.getUserId());
                    userNameEdit.setText(user.getUserName());
                    bioEdit.setText(user.getBio());
                    if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                        ImageUtils.loadImage(user.getAvatar(), profileImageView);
                    }
                }
            }
            @Override
            public void onError(Exception e) {
                Toast.makeText(EditProfileActivity.this, "Tải thông tin người dùng thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfileInfo() {
        String userName = userNameEdit.getText().toString().trim();
        String bio = bioEdit.getText().toString().trim();

        Map<String, Object> updates = new HashMap<>();
        updates.put("userName", userName);
        updates.put("bio", bio);

        userRepository.updateUser(firebaseUser.getUid(), updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(EditProfileActivity.this, "Hồ sơ đã được cập nhật thành công!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, "Cập nhật hồ sơ thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void changePassword() {
        String currentPassword = currentPasswordEdit.getText().toString();
        String newPassword = newPasswordEdit.getText().toString();
        String confirmPassword = confirmPasswordEdit.getText().toString();

        if (TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Vui lòng điền đầy đủ các trường mật khẩu.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "Mật khẩu mới phải có ít nhất 6 ký tự.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu mới không khớp.", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), currentPassword);

        firebaseUser.reauthenticate(credential).addOnCompleteListener(reauthTask -> {
            if (reauthTask.isSuccessful()) {
                firebaseUser.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        Toast.makeText(EditProfileActivity.this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                        currentPasswordEdit.setText("");
                        newPasswordEdit.setText("");
                        confirmPasswordEdit.setText("");
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Đổi mật khẩu thất bại: " + updateTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(EditProfileActivity.this, "Xác thực lại thất bại: " + reauthTask.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
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
            MediaManager.get().upload(imageUri).callback(new UploadCallback() {
                @Override
                public void onSuccess(String requestId, Map resultData) {
                    progressDialog.dismiss();
                    String imageUrl = (String) resultData.get("secure_url");
                    updateAvatarInFirestore(imageUrl);
                }

                @Override
                public void onError(String requestId, ErrorInfo error) {
                    progressDialog.dismiss();
                    Toast.makeText(EditProfileActivity.this, "Tải ảnh lên thất bại: " + error.getDescription(), Toast.LENGTH_LONG).show();
                }
                @Override public void onStart(String requestId) {}
                @Override public void onProgress(String requestId, long bytes, long totalBytes) {}
                @Override public void onReschedule(String requestId, ErrorInfo error) {}
            }).dispatch();
        }
    }

    private void updateAvatarInFirestore(String imageUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("avatar", imageUrl);
        userRepository.updateUser(firebaseUser.getUid(), updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(EditProfileActivity.this, "Cập nhật avatar thành công!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, "Cập nhật URL avatar thất bại.", Toast.LENGTH_SHORT).show());
    }
}

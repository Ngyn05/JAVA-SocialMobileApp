package vn.edu.ueh.socialapplication.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import vn.edu.ueh.socialapplication.R;
import vn.edu.ueh.socialapplication.data.repository.UserRepository;

public class RegisterActivity extends AppCompatActivity {

    private EditText userNameInput, userIdInput, emailInput, passwordInput;
    private Button registerButton;
    private TextView loginLink;
    private ImageView backButton;

    private FirebaseAuth mAuth;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userNameInput = findViewById(R.id.userName_input);
        userIdInput = findViewById(R.id.userId_input);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        registerButton = findViewById(R.id.register_button);
        loginLink = findViewById(R.id.login_link);
        backButton = findViewById(R.id.back_button_register);

        mAuth = FirebaseAuth.getInstance();
        userRepository = new UserRepository();

        registerButton.setOnClickListener(v -> registerUser());
        loginLink.setOnClickListener(v -> finish()); 
        backButton.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String userName = userNameInput.getText().toString().trim();
        String userId = userIdInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(userId) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        userRepository.checkUserIdExists(userId, new UserRepository.OnUserIdCheckListener() {
            @Override
            public void onResult(boolean isTaken) {
                if (isTaken) {
                    Toast.makeText(RegisterActivity.this, "User ID này đã có người sử dụng. Vui lòng chọn một ID khác.", Toast.LENGTH_LONG).show();
                } else {
                    createUserAndSendVerificationLink(userName, userId, email, password);
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(RegisterActivity.this, "Lỗi khi kiểm tra User ID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createUserAndSendVerificationLink(String userName, String userId, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            firebaseUser.sendEmailVerification();

                            // DO NOT save to database yet. Pass data to the verification screen.
                            Intent intent = new Intent(RegisterActivity.this, EmailVerificationActivity.class);
                            intent.putExtra("uid", firebaseUser.getUid());
                            intent.putExtra("userName", userName);
                            intent.putExtra("userId", userId);
                            intent.putExtra("email", email);
                            startActivity(intent);
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}

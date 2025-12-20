package vn.edu.ueh.socialapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EmailVerificationActivity extends AppCompatActivity {

    private Button resendEmailButton, backToRegisterButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Handler handler;
    private Runnable verificationChecker;

    private String uid, userName, userId, email;

    private static final String TAG = "EmailVerification";
    private static final int COOLDOWN_SECONDS = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        // Get user data passed from RegisterActivity
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        userName = intent.getStringExtra("userName");
        userId = intent.getStringExtra("userId");
        email = intent.getStringExtra("email");

        resendEmailButton = findViewById(R.id.resend_email_button);
        backToRegisterButton = findViewById(R.id.back_to_register_button);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        resendEmailButton.setOnClickListener(v -> resendVerificationEmail());
        backToRegisterButton.setOnClickListener(v -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                user.delete();
            }
            finish(); 
        });

        handler = new Handler(Looper.getMainLooper());
        verificationChecker = new Runnable() {
            @Override
            public void run() {
                checkEmailVerificationStatus();
                handler.postDelayed(this, 3000); // Check every 3 seconds
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        handler.post(verificationChecker);
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(verificationChecker);
    }

    private void checkEmailVerificationStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (user.isEmailVerified()) {
                    handler.removeCallbacks(verificationChecker);
                    saveUserDataToFirestore();
                }
            });
        }
    }

    private void saveUserDataToFirestore() {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("userName", userName);
        userMap.put("userId", userId);
        userMap.put("email", email);
        userMap.put("avatar", "");
        userMap.put("bio", "");

        db.collection("users").document(uid).set(userMap)
                .addOnCompleteListener(saveTask -> {
                    if (saveTask.isSuccessful()) {
                        Toast.makeText(EmailVerificationActivity.this, "Xác thực và đăng ký thành công!", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(EmailVerificationActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(EmailVerificationActivity.this, "Lưu dữ liệu người dùng thất bại.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            resendEmailButton.setEnabled(false);
            user.sendEmailVerification().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(EmailVerificationActivity.this, "Email xác thực đã được gửi lại.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "sendEmailVerification failed", task.getException());
                    Toast.makeText(EmailVerificationActivity.this, "Không thể gửi lại email, vui lòng thử lại sau ít phút.", Toast.LENGTH_LONG).show();
                }
                // Re-enable the button after a cooldown period
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    resendEmailButton.setEnabled(true);
                }, COOLDOWN_SECONDS * 1000);
            });
        }
    }
}

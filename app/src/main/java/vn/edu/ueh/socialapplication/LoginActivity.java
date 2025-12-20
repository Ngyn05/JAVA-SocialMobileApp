package vn.edu.ueh.socialapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "GoogleSignIn";

    private EditText emailInput, passwordInput;
    private Button loginButton, googleLoginButton;
    private TextView signupLink, forgotPasswordLink;
    private ImageView backButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        googleLoginButton = findViewById(R.id.google_login_button);
        signupLink = findViewById(R.id.signup_link);
        forgotPasswordLink = findViewById(R.id.forgot_password_link);
        backButton = findViewById(R.id.back_button_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Set listeners
        loginButton.setOnClickListener(v -> loginUser());
        signupLink.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
        forgotPasswordLink.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));
        googleLoginButton.setOnClickListener(v -> signInWithGoogle());
        backButton.setOnClickListener(v -> finish());
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            // Email is verified, proceed to home
                            navigateToHome();
                        } else {
                            // Email is not verified
                            Toast.makeText(LoginActivity.this, "Vui lòng xác thực email của bạn trước khi đăng nhập.", Toast.LENGTH_LONG).show();
                            mAuth.signOut(); // Sign out to prevent inconsistent state
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(TAG, "Đăng nhập Google thất bại", e);
                Toast.makeText(this, "Đăng nhập Google thất bại", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        checkIfNewUserAndSave(user);
                    } else {
                        Toast.makeText(LoginActivity.this, "Xác thực thất bại.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkIfNewUserAndSave(FirebaseUser firebaseUser) {
        String authUid = firebaseUser.getUid();
        db.collection("users").document(authUid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (!document.exists()) {
                    // New user, save data to Firestore
                    Map<String, Object> user = new HashMap<>();
                    user.put("userName", firebaseUser.getDisplayName());
                    user.put("userId", ""); // User can set this custom ID later
                    user.put("email", firebaseUser.getEmail());
                    user.put("avatar", firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : "");
                    user.put("bio", "");

                    db.collection("users").document(authUid).set(user)
                            .addOnCompleteListener(saveTask -> {
                                if (saveTask.isSuccessful()) {
                                    navigateToHome();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Lưu dữ liệu người dùng thất bại.", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    // Existing user
                    navigateToHome();
                }
            } else {
                Toast.makeText(LoginActivity.this, "Lỗi khi kiểm tra dữ liệu người dùng.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToHome() {
        Toast.makeText(LoginActivity.this, "Đăng nhập thành công.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}

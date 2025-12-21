package vn.edu.ueh.socialapplication.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import vn.edu.ueh.socialapplication.R;
import vn.edu.ueh.socialapplication.data.repository.UserRepository;
import vn.edu.ueh.socialapplication.home.HomeActivity;

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
    private UserRepository userRepository;

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
        userRepository = new UserRepository();

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
                            navigateToHome();
                        } else {
                            Toast.makeText(LoginActivity.this, "Vui lòng xác thực email của bạn trước khi đăng nhập.", Toast.LENGTH_LONG).show();
                            mAuth.signOut();
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
                    // This is a new user, create a profile in Firestore
                    generateUniqueUserIdAndSave(firebaseUser);
                } else {
                    // Existing user, just navigate to home
                    navigateToHome();
                }
            } else {
                Toast.makeText(LoginActivity.this, "Lỗi khi kiểm tra dữ liệu người dùng.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateUniqueUserIdAndSave(FirebaseUser firebaseUser) {
        String email = firebaseUser.getEmail();
        String baseUserId = "";
        if (email != null && email.contains("@")) {
            baseUserId = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        }

        // Check if this baseUserId is already taken
        String finalBaseUserId = baseUserId;
        userRepository.checkUserIdExists(baseUserId, new UserRepository.OnUserIdCheckListener() {
            @Override
            public void onResult(boolean isTaken) {
                String finalUserId = finalBaseUserId;
                if (isTaken) {
                    // If taken, append a random number
                    finalUserId = finalBaseUserId + new Random().nextInt(1000);
                }
                saveNewUserData(firebaseUser, finalUserId);
            }

            @Override
            public void onError(Exception e) {
                // If checking fails, proceed with a potentially non-unique ID
                saveNewUserData(firebaseUser, finalBaseUserId);
            }
        });
    }

    private void saveNewUserData(FirebaseUser firebaseUser, String finalUserId) {
        Map<String, Object> user = new HashMap<>();
        user.put("userName", firebaseUser.getDisplayName());
        user.put("userId", finalUserId);
        user.put("email", firebaseUser.getEmail());
        user.put("avatar", firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : "");
        user.put("bio", "");

        db.collection("users").document(firebaseUser.getUid()).set(user)
                .addOnCompleteListener(saveTask -> {
                    if (saveTask.isSuccessful()) {
                        navigateToHome();
                    } else {
                        Toast.makeText(LoginActivity.this, "Lưu dữ liệu người dùng thất bại.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToHome() {
        getAndStoreFcmToken();
        Toast.makeText(LoginActivity.this, "Đăng nhập thành công.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void getAndStoreFcmToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();

                    // Log and toast
                    String msg = "FCM Registration Token: " + token;
                    Log.d(TAG, msg);

                    // Save the token to Firestore
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        db.collection("users").document(firebaseUser.getUid())
                                .update("fcmToken", token)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "FCM token updated successfully"))
                                .addOnFailureListener(e -> Log.w(TAG, "Error updating FCM token", e));
                    }
                });
    }
}
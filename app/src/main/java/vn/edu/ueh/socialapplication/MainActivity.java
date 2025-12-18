package vn.edu.ueh.socialapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            // User is already logged in, show splash screen for 3 seconds
            setContentView(R.layout.activity_splash);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                startActivity(new Intent(MainActivity.this, HomeActivity.class));
                finish();
            }, SPLASH_DELAY);
        } else {
            // No user is logged in, go directly to WelcomeActivity without delay
            startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
            finish();
        }
    }
}

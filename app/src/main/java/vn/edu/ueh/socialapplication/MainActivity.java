package vn.edu.ueh.socialapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.android.MediaManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

import vn.edu.ueh.socialapplication.home.HomeActivity;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Cloudinary
        try {
            Map<String, String> config = new HashMap<>();
            // config.put("cloud_name", "cloud_name");
            // config.put("api_key", "api_key");
            // config.put("api_secret", "api_secret");
            // MediaManager.init(this, config);
        } catch (Exception e) {
            // Already initialized
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            // User is already logged in, show splash screen for 2 seconds
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

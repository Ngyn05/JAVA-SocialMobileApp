package vn.edu.ueh.socialapplication;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        backButton = findViewById(R.id.back_button_profile);

        backButton.setOnClickListener(v -> {
            // Finish the activity and go back to the previous screen
            finish();
        });
    }
}

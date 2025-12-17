package vn.edu.ueh.socialapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

import vn.edu.ueh.socialapplication.home.HomeActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        try {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "dt0drhz8d");
            config.put("api_key", "893675145317651");
            config.put("api_secret", "ktAd44YuxTMfeXjaIOEy4tcUsnI");

            MediaManager.init(this, config);
        } catch (Exception e) {
            // Đã init rồi thì thôi, không làm gì cả
        }

        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);

        // 3. Đóng MainActivity (Optional)
        // Nếu bạn không muốn user bấm nút Back quay lại màn hình này thì gọi lệnh finish()
        finish();
    }
}
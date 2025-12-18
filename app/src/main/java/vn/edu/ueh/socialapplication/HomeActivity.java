package vn.edu.ueh.socialapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    // The RecyclerView here is for the future main feed, not for search results anymore.
    private RecyclerView recyclerView;

    private ImageView profileImage, searchIcon;

    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        profileImage = findViewById(R.id.profile_image);
        searchIcon = findViewById(R.id.search_icon);
        recyclerView = findViewById(R.id.recycler_view);

        userRepository = new UserRepository();

        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCurrentUser();
    }

    private void setupListeners() {
        // When search icon is clicked, open the dedicated SearchActivity
        searchIcon.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, SearchActivity.class)));

        profileImage.setOnClickListener(this::showProfileMenu);
    }

    private void showProfileMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenuInflater().inflate(R.menu.profile_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_profile) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                return true;
            } else if (itemId == R.id.menu_account) {
                startActivity(new Intent(HomeActivity.this, EditProfileActivity.class));
                return true;
            } else if (itemId == R.id.menu_logout) {
                logoutUser();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void loadCurrentUser() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            userRepository.getUser(uid, new UserRepository.OnUserLoadedListener() {
                @Override
                public void onUserLoaded(User user) {
                    updateUI(user);
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(HomeActivity.this, "User profile not found. Logging out.", Toast.LENGTH_LONG).show();
                    logoutUser();
                }
            });
        }
    }

    private void updateUI(User user) {
        if (user != null && user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            ImageUtils.loadImage(user.getAvatar(), profileImage);
        } else {
            profileImage.setImageResource(R.drawable.ic_account_circle);
        }
    }
}

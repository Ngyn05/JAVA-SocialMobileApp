package vn.edu.ueh.socialapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;

    private ImageView profileImage;
    private EditText searchBar;

    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        profileImage = findViewById(R.id.profile_image);
        searchBar = findViewById(R.id.search_bar);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userList = new ArrayList<>();
        userAdapter = new UserAdapter(this, userList);
        recyclerView.setAdapter(userAdapter);

        userRepository = new UserRepository();

        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Load or refresh user data every time the activity is resumed.
        // This ensures the avatar is updated after being changed in EditProfileActivity.
        loadCurrentUser();
    }

    private void setupListeners() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

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

    private void searchUsers(String s) {
        userRepository.searchUsers(s, new UserRepository.OnUsersSearchedListener() {
            @Override
            public void onUsersSearched(List<User> users) {
                userList.clear();
                userList.addAll(users);
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(HomeActivity.this, "Search failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
        // If user has a valid avatar URL, load it. Otherwise, set the default placeholder.
        if (user != null && user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            ImageUtils.loadImage(user.getAvatar(), profileImage);
        } else {
            profileImage.setImageResource(R.drawable.ic_account_circle);
        }
    }
}

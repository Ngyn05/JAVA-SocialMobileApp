package vn.edu.ueh.socialapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
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
    private ProgressBar progressBar;
    private TextView noResultsText;

    private UserRepository userRepository;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private static final long DEBOUNCE_DELAY = 500; // 500ms

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize views
        profileImage = findViewById(R.id.profile_image);
        searchBar = findViewById(R.id.search_bar_home);
        recyclerView = findViewById(R.id.recycler_view_home);
        progressBar = findViewById(R.id.progress_bar_home);
        noResultsText = findViewById(R.id.no_results_text_home);

        userRepository = new UserRepository();

        // Setup RecyclerView
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(this, userList);
        recyclerView.setAdapter(userAdapter);

        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCurrentUser();
    }

    private void setupListeners() {
        profileImage.setOnClickListener(this::showProfileMenu);

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchRunnable = () -> searchUsers(s.toString());
                searchHandler.postDelayed(searchRunnable, DEBOUNCE_DELAY);
            }
        });
    }

    private void searchUsers(String query) {
        if (query.isEmpty()) {
            userList.clear();
            userAdapter.notifyDataSetChanged();
            noResultsText.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE); // Hide RecyclerView when query is empty
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        noResultsText.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE); // Show RecyclerView for results

        userRepository.searchUsers(query, new UserRepository.OnUsersSearchedListener() {
            @Override
            public void onUsersSearched(List<User> users) {
                progressBar.setVisibility(View.GONE);
                userList.clear();
                userList.addAll(users);
                userAdapter.notifyDataSetChanged();

                if (users.isEmpty()) {
                    noResultsText.setVisibility(View.VISIBLE);
                } else {
                    noResultsText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(Exception e) {
                progressBar.setVisibility(View.GONE);
                noResultsText.setVisibility(View.VISIBLE);
                noResultsText.setText("Lỗi khi tìm kiếm.");
            }
        });
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
                    Toast.makeText(HomeActivity.this, "Không tìm thấy hồ sơ người dùng. Đang đăng xuất.", Toast.LENGTH_LONG).show();
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

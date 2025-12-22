package vn.edu.ueh.socialapplication.search;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import vn.edu.ueh.socialapplication.R;
import vn.edu.ueh.socialapplication.data.model.User;
import vn.edu.ueh.socialapplication.profile.OtherProfileActivity;
import vn.edu.ueh.socialapplication.data.repository.UserRepository;

public class SearchActivity extends AppCompatActivity implements UserAdapter.OnUserClickListener {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;

    private EditText searchBar;
    private ImageView clearTextIcon, backButton;
    private ProgressBar progressBar;
    private TextView noResultsText;

    private UserRepository userRepository;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private String currentUserId;
    private static final long DEBOUNCE_DELAY = 500; // 500ms delay

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Initialize views
        recyclerView = findViewById(R.id.recycler_view_search);
        searchBar = findViewById(R.id.search_bar_input);
        clearTextIcon = findViewById(R.id.clear_text_icon);
        backButton = findViewById(R.id.back_button);
        progressBar = findViewById(R.id.progress_bar);
        noResultsText = findViewById(R.id.no_results_text);

        userRepository = new UserRepository();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Setup RecyclerView
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(this, userList, this);
        recyclerView.setAdapter(userAdapter);

        // Setup Listeners
        backButton.setOnClickListener(v -> finish());
        clearTextIcon.setOnClickListener(v -> searchBar.setText(""));

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                clearTextIcon.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
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
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        noResultsText.setVisibility(View.GONE);

        userRepository.searchUsers(query, currentUserId, new UserRepository.OnUsersSearchedListener() {
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

    @Override
    public void onUserClick(User user) {
        Intent intent = new Intent(this, OtherProfileActivity.class);
        intent.putExtra("USER_ID", user.getUid());
        startActivity(intent);
    }
}
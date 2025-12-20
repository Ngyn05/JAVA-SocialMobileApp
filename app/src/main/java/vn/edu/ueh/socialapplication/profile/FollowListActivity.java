package vn.edu.ueh.socialapplication.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.edu.ueh.socialapplication.R;
import vn.edu.ueh.socialapplication.data.model.User;
import vn.edu.ueh.socialapplication.data.repository.UserRepository;
import vn.edu.ueh.socialapplication.follow.FollowRepository;
import vn.edu.ueh.socialapplication.follow.FollowViewModel;
import vn.edu.ueh.socialapplication.search.UserAdapter;

public class FollowListActivity extends AppCompatActivity implements UserAdapter.OnUserClickListener {

    private String id;
    private String title;
    private List<String> idList;
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;
    private FollowViewModel followViewModel;
    private ImageView backButton;
    private TextView toolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_list);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        title = intent.getStringExtra("title");

        toolbarTitle = findViewById(R.id.toolbar_title);
        backButton = findViewById(R.id.back_button_follow);
        recyclerView = findViewById(R.id.recycler_view_follow);
        
        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false); // We use custom back button

        backButton.setOnClickListener(v -> finish());
        
        if (title != null) {
            if (title.equals("followers")) {
                toolbarTitle.setText("Người theo dõi");
            } else if (title.equals("following")) {
                toolbarTitle.setText("Đang theo dõi");
            }
        }

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(this, userList, this);
        recyclerView.setAdapter(userAdapter);

        idList = new ArrayList<>();

        // Initialize ViewModel
        FollowRepository followRepository = new FollowRepository();
        UserRepository userRepository = new UserRepository();
        FollowViewModel.Factory factory = new FollowViewModel.Factory(followRepository, userRepository);
        followViewModel = new ViewModelProvider(this, factory).get(FollowViewModel.class);

        showUsers();
    }

    private void showUsers() {
        if (title.equals("followers")) {
            followViewModel.getFollowers(id).observe(this, users -> {
                userList.clear();
                userList.addAll(users);
                userAdapter.notifyDataSetChanged();
            });
        } else if (title.equals("following")) {
            followViewModel.getFollowing(id).observe(this, users -> {
                userList.clear();
                userList.addAll(users);
                userAdapter.notifyDataSetChanged();
            });
        }
    }

    @Override
    public void onUserClick(User user) {
        Intent intent = new Intent(this, OtherProfileActivity.class);
        intent.putExtra("USER_ID", user.getUserId());
        startActivity(intent);
    }
}

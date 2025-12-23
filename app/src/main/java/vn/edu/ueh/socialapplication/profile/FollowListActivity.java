package vn.edu.ueh.socialapplication.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.edu.ueh.socialapplication.R;
import vn.edu.ueh.socialapplication.data.model.User;

public class FollowListActivity extends AppCompatActivity {

    private FollowListViewModel viewModel;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView toolbarTitle;
    private FollowListAdapter adapter;
    private List<User> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_list);

        viewModel = new ViewModelProvider(this).get(FollowListViewModel.class);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbarTitle = findViewById(R.id.toolbar_title);
        recyclerView = findViewById(R.id.recycler_view_follow_list);
        progressBar = findViewById(R.id.progress_bar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FollowListAdapter(this, userList);
        recyclerView.setAdapter(adapter);

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        List<String> userIds = intent.getStringArrayListExtra("userIds");

        toolbarTitle.setText(title);

        observeViewModel();
        if (userIds != null && !userIds.isEmpty()) {
            progressBar.setVisibility(View.VISIBLE);
            viewModel.loadUsers(userIds);
        }
    }

    private void observeViewModel() {
        viewModel.getUsers().observe(this, users -> {
            progressBar.setVisibility(View.GONE);
            userList.clear();
            userList.addAll(users);
            adapter.notifyDataSetChanged();
        });

        viewModel.getError().observe(this, error -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
    }
}
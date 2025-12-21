package vn.edu.ueh.socialapplication.notification;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.edu.ueh.socialapplication.R;
import vn.edu.ueh.socialapplication.data.model.Notification;

public class NotificationActivity extends AppCompatActivity {

    private NotificationViewModel notificationViewModel;
    private RecyclerView rvNotifications;
    private ProgressBar progressBar;
    private NotificationAdapter adapter;
    private List<Notification> notificationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        notificationViewModel = new ViewModelProvider(this).get(NotificationViewModel.class);

        rvNotifications = findViewById(R.id.rvNotifications);
        progressBar = findViewById(R.id.progress_bar);

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(this, notificationList);
        rvNotifications.setAdapter(adapter);

        observeViewModel();
        notificationViewModel.fetchNotifications();
    }

    private void observeViewModel() {
        notificationViewModel.getNotifications().observe(this, notifications -> {
            progressBar.setVisibility(View.GONE);
            notificationList.clear();
            notificationList.addAll(notifications);
            adapter.notifyDataSetChanged();
        });

        notificationViewModel.getError().observe(this, error -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
    }
}
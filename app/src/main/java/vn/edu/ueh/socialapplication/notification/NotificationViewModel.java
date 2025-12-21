package vn.edu.ueh.socialapplication.notification;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import vn.edu.ueh.socialapplication.data.model.Notification;
import vn.edu.ueh.socialapplication.data.repository.NotificationRepository;

public class NotificationViewModel extends ViewModel {
    private final NotificationRepository notificationRepository;
    private final MutableLiveData<List<Notification>> notifications = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public NotificationViewModel() {
        this.notificationRepository = new NotificationRepository();
    }

    public LiveData<List<Notification>> getNotifications() {
        return notifications;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void fetchNotifications() {
        Query query = notificationRepository.getNotifications();
        if (query != null) {
            query.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<Notification> notificationList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Notification notification = document.toObject(Notification.class);
                        notification.setId(document.getId());
                        notificationList.add(notification);
                    }
                    notifications.postValue(notificationList);
                } else {
                    error.postValue("Error fetching notifications");
                }
            });
        }
    }

    public void markNotificationsAsRead() {
        notificationRepository.markNotificationsAsRead();
    }
}
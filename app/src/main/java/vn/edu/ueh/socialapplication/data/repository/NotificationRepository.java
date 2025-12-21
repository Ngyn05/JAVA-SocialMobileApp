package vn.edu.ueh.socialapplication.data.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import vn.edu.ueh.socialapplication.data.model.Notification;

public class NotificationRepository {
    private final CollectionReference notificationsCollection;

    public NotificationRepository() {
        this.notificationsCollection = FirebaseFirestore.getInstance().collection("notifications");
    }

    public void sendNotification(Notification notification) {
        notificationsCollection.add(notification);
    }

    public Query getNotifications() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return notificationsCollection
                    .whereEqualTo("userId", currentUser.getUid())
                    .orderBy("timestamp", Query.Direction.DESCENDING);
        }
        return null;
    }
}
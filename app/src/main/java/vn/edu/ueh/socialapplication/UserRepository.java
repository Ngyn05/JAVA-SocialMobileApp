package vn.edu.ueh.socialapplication;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import vn.edu.ueh.socialapplication.data.model.User;

public class UserRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnUserLoadedListener {
        void onUserLoaded(User user);
        void onError(Exception e);
    }

    public interface OnUsersSearchedListener {
        void onUsersSearched(List<User> users);
        void onError(Exception e);
    }

    public interface OnUserIdCheckListener {
        void onResult(boolean isTaken);
        void onError(Exception e);
    }

    public void getUser(String uid, final OnUserLoadedListener listener) {
        db.collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            User user = document.toObject(User.class);
                            listener.onUserLoaded(user);
                        } else {
                            listener.onError(new Exception("User not found"));
                        }
                    } else {
                        listener.onError(task.getException());
                    }
                });
    }

    public Task<Void> updateUser(String uid, Map<String, Object> updates) {
        return db.collection("users").document(uid).update(updates);
    }

    public void checkUserIdExists(String userId, final OnUserIdCheckListener listener) {
        db.collection("users").whereEqualTo("userId", userId).limit(1).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onResult(!task.getResult().isEmpty());
                    } else {
                        listener.onError(task.getException());
                    }
                });
    }

    public void searchUsers(String queryText, final OnUsersSearchedListener listener) {
        if (queryText == null || queryText.isEmpty()) {
            listener.onUsersSearched(new ArrayList<>());
            return;
        }

        Query queryByUserName = db.collection("users")
                .orderBy("userName")
                .startAt(queryText)
                .endAt(queryText + "\uf8ff");

        Query queryByUserId = db.collection("users")
                .orderBy("userId")
                .startAt(queryText)
                .endAt(queryText + "\uf8ff");

        Task<QuerySnapshot> userNameTask = queryByUserName.get();
        Task<QuerySnapshot> userIdTask = queryByUserId.get();

        Tasks.whenAllSuccess(userNameTask, userIdTask).addOnSuccessListener(results -> {
            // Use a LinkedHashMap to maintain order and remove duplicates based on document ID
            Map<String, User> userMap = new LinkedHashMap<>();

            // Process username results
            QuerySnapshot userNameSnapshot = (QuerySnapshot) results.get(0);
            for (DocumentSnapshot doc : userNameSnapshot.getDocuments()) {
                userMap.put(doc.getId(), doc.toObject(User.class));
            }

            // Process userId results
            QuerySnapshot userIdSnapshot = (QuerySnapshot) results.get(1);
            for (DocumentSnapshot doc : userIdSnapshot.getDocuments()) {
                userMap.put(doc.getId(), doc.toObject(User.class));
            }

            listener.onUsersSearched(new ArrayList<>(userMap.values()));

        }).addOnFailureListener(listener::onError);
    }
}

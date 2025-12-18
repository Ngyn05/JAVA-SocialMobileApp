package vn.edu.ueh.socialapplication;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
                        // If the result is not empty, the userId is taken.
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

        Query query = db.collection("users")
                .orderBy("userName")
                .startAt(queryText)
                .endAt(queryText + "\uf8ff");

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<User> userList = new ArrayList<>();
                QuerySnapshot snapshot = task.getResult();
                if (snapshot != null) {
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        userList.add(doc.toObject(User.class));
                    }
                }
                listener.onUsersSearched(userList);
            } else {
                listener.onError(task.getException());
            }
        });
    }
}

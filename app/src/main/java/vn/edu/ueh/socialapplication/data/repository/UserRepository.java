package vn.edu.ueh.socialapplication.data.repository;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

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

    public interface OnMultipleUsersLoadedListener {
        void onUsersLoaded(List<User> users);
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

    public interface OnFollowCompleteListener {
        void onSuccess();
        void onError(String errorMessage);
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

    public void getUsers(List<String> userIds, final OnMultipleUsersLoadedListener listener) {
        if (userIds == null || userIds.isEmpty()) {
            listener.onUsersLoaded(new ArrayList<>());
            return;
        }

        db.collection("users").whereIn(FieldPath.documentId(), userIds).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<User> users = task.getResult().toObjects(User.class);
                        listener.onUsersLoaded(users);
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

    public void searchUsers(String queryText, String currentUserId, final OnUsersSearchedListener listener) {
        if (queryText == null || queryText.isEmpty()) {
            db.collection("users").get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<User> users = new ArrayList<>();
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            if (doc.getId().equals(currentUserId)) continue;
                            User user = doc.toObject(User.class);
                            if (user != null) {
                                users.add(user);
                            }
                        }
                        listener.onUsersSearched(users);
                    })
                    .addOnFailureListener(listener::onError);
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
            Map<String, User> userMap = new LinkedHashMap<>();

            QuerySnapshot userNameSnapshot = (QuerySnapshot) results.get(0);
            for (DocumentSnapshot doc : userNameSnapshot.getDocuments()) {
                if (doc.getId().equals(currentUserId)) continue; // Skip current user
                User user = doc.toObject(User.class);
                if (user != null) {
                    userMap.put(doc.getId(), user);
                }
            }

            QuerySnapshot userIdSnapshot = (QuerySnapshot) results.get(1);
            for (DocumentSnapshot doc : userIdSnapshot.getDocuments()) {
                if (doc.getId().equals(currentUserId)) continue; // Skip current user
                User user = doc.toObject(User.class);
                if (user != null) {
                    userMap.put(doc.getId(), user);
                }
            }

            listener.onUsersSearched(new ArrayList<>(userMap.values()));

        }).addOnFailureListener(listener::onError);
    }

    public void followUser(String currentUserId, String targetUserId, final OnFollowCompleteListener listener) {
        WriteBatch batch = db.batch();

        DocumentReference currentUserRef = db.collection("users").document(currentUserId);
        batch.update(currentUserRef, "following", FieldValue.arrayUnion(targetUserId));

        DocumentReference targetUserRef = db.collection("users").document(targetUserId);
        batch.update(targetUserRef, "followers", FieldValue.arrayUnion(currentUserId));

        batch.commit().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                listener.onSuccess();
            } else {
                listener.onError(task.getException().getMessage());
            }
        });
    }

    public void unfollowUser(String currentUserId, String targetUserId, final OnFollowCompleteListener listener) {
        WriteBatch batch = db.batch();

        DocumentReference currentUserRef = db.collection("users").document(currentUserId);
        batch.update(currentUserRef, "following", FieldValue.arrayRemove(targetUserId));

        DocumentReference targetUserRef = db.collection("users").document(targetUserId);
        batch.update(targetUserRef, "followers", FieldValue.arrayRemove(currentUserId));

        batch.commit().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                listener.onSuccess();
            } else {
                listener.onError(task.getException().getMessage());
            }
        });
    }
}

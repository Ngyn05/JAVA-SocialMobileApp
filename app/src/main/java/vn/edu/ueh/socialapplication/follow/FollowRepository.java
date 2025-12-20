package vn.edu.ueh.socialapplication.follow;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.edu.ueh.socialapplication.data.model.User;

public class FollowRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Task<Void> followUser(String currentUserId, String targetUserId) {
        WriteBatch batch = db.batch();

        Map<String, Object> followerData = new HashMap<>();
        followerData.put("timestamp", System.currentTimeMillis());

        Map<String, Object> followingData = new HashMap<>();
        followingData.put("timestamp", System.currentTimeMillis());

        // Add to target user's followers
        batch.set(db.collection("users").document(targetUserId)
                .collection("followers").document(currentUserId), followerData);

        // Add to current user's following
        batch.set(db.collection("users").document(currentUserId)
                .collection("following").document(targetUserId), followingData);

        // Optional: Create notification (handled by Notification module usually, but we can trigger it here or Cloud Functions)

        return batch.commit();
    }

    public Task<Void> unfollowUser(String currentUserId, String targetUserId) {
        WriteBatch batch = db.batch();

        // Remove from target user's followers
        batch.delete(db.collection("users").document(targetUserId)
                .collection("followers").document(currentUserId));

        // Remove from current user's following
        batch.delete(db.collection("users").document(currentUserId)
                .collection("following").document(targetUserId));

        return batch.commit();
    }

    public Task<Boolean> isFollowing(String currentUserId, String targetUserId) {
        return db.collection("users").document(currentUserId)
                .collection("following").document(targetUserId).get()
                .continueWith(task -> task.isSuccessful() && task.getResult().exists());
    }

    public Task<Integer> getFollowersCount(String userId) {
        return db.collection("users").document(userId).collection("followers").get()
                .continueWith(task -> task.isSuccessful() ? task.getResult().size() : 0);
    }

    public Task<Integer> getFollowingCount(String userId) {
        return db.collection("users").document(userId).collection("following").get()
                .continueWith(task -> task.isSuccessful() ? task.getResult().size() : 0);
    }
    
    public Task<List<String>> getFollowersIds(String userId) {
         return db.collection("users").document(userId).collection("followers").get()
                .continueWith(task -> {
                    List<String> ids = new ArrayList<>();
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            ids.add(doc.getId());
                        }
                    }
                    return ids;
                });
    }

    public Task<List<String>> getFollowingIds(String userId) {
        return db.collection("users").document(userId).collection("following").get()
               .continueWith(task -> {
                   List<String> ids = new ArrayList<>();
                   if (task.isSuccessful()) {
                       for (DocumentSnapshot doc : task.getResult()) {
                           ids.add(doc.getId());
                       }
                   }
                   return ids;
               });
   }
}

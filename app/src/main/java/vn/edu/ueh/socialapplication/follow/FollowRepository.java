package vn.edu.ueh.socialapplication.follow;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

public class FollowRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Thực hiện follow người dùng. Cập nhật mảng followers của đối phương và following của mình.
     */
    public Task<Void> followUser(String currentUserId, String targetUserId) {
        WriteBatch batch = db.batch();

        // Thêm currentUserId vào mảng followers của người được follow
        batch.update(db.collection("users").document(targetUserId),
                "followers", FieldValue.arrayUnion(currentUserId));

        // Thêm targetUserId vào mảng following của người đang thực hiện follow
        batch.update(db.collection("users").document(currentUserId),
                "following", FieldValue.arrayUnion(targetUserId));

        return batch.commit();
    }

    /**
     * Thực hiện bỏ follow. Xóa UID khỏi mảng tương ứng.
     */
    public Task<Void> unfollowUser(String currentUserId, String targetUserId) {
        WriteBatch batch = db.batch();

        batch.update(db.collection("users").document(targetUserId),
                "followers", FieldValue.arrayRemove(currentUserId));

        batch.update(db.collection("users").document(currentUserId),
                "following", FieldValue.arrayRemove(targetUserId));

        return batch.commit();
    }

    /**
     * Kiểm tra xem currentUserId có đang theo dõi targetUserId hay không.
     */
    public Task<Boolean> isFollowing(String currentUserId, String targetUserId) {
        return db.collection("users").document(currentUserId).get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        List<String> following = (List<String>) task.getResult().get("following");
                        return following != null && following.contains(targetUserId);
                    }
                    return false;
                });
    }

    /**
     * Lấy số lượng người theo dõi từ mảng 'followers'.
     */
    public Task<Integer> getFollowersCount(String userId) {
        return db.collection("users").document(userId).get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        List<String> followers = (List<String>) task.getResult().get("followers");
                        return followers != null ? followers.size() : 0;
                    }
                    return 0;
                });
    }

    /**
     * Lấy số lượng người đang theo dõi từ mảng 'following'.
     */
    public Task<Integer> getFollowingCount(String userId) {
        return db.collection("users").document(userId).get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        List<String> following = (List<String>) task.getResult().get("following");
                        return following != null ? following.size() : 0;
                    }
                    return 0;
                });
    }

    /**
     * Lấy danh sách ID người theo dõi.
     */
    public Task<List<String>> getFollowersIds(String userId) {
        return db.collection("users").document(userId).get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<String> followers = (List<String>) task.getResult().get("followers");
                        return followers != null ? followers : new ArrayList<>();
                    }
                    return new ArrayList<>();
                });
    }

    /**
     * Lấy danh sách ID người đang theo dõi.
     */
    public Task<List<String>> getFollowingIds(String userId) {
        return db.collection("users").document(userId).get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<String> following = (List<String>) task.getResult().get("following");
                        return following != null ? following : new ArrayList<>();
                    }
                    return new ArrayList<>();
                });
    }
}

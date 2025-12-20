package vn.edu.ueh.socialapplication.post;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.edu.ueh.socialapplication.data.model.Comment;
import vn.edu.ueh.socialapplication.data.model.Post;
import vn.edu.ueh.socialapplication.data.model.User;
import vn.edu.ueh.socialapplication.data.repository.CommentRepository;

public class PostDetailViewModel extends ViewModel {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    // Đối tượng User từ class của bạn
    private User currentUser;

    private final MutableLiveData<List<Comment>> commentsData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> commentPostStatus = new MutableLiveData<>();

    private final MutableLiveData<Post> postData = new MutableLiveData<>();

    public LiveData<Post> getPostData() {
        return postData;
    }

    // 1. Hàm khởi tạo (Constructor) - Tự động chạy khi mở màn hình Detail
    public PostDetailViewModel() {
        loadCurrentUserInfo();
    }

    // Lấy thông tin username từ collection "users" dựa trên UID
    private void loadCurrentUserInfo() {
        if (firebaseUser != null) {
            db.collection("users").document(firebaseUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Chuyển document thành object User của bạn
                            currentUser = documentSnapshot.toObject(User.class);
                        }
                    });
        }
    }

    public LiveData<List<Comment>> getCommentsData() {
        return commentsData;
    }

    public LiveData<Boolean> getCommentPostStatus() {
        return commentPostStatus;
    }

    public void listenForComments(String postId) {
        db.collection("posts").document(postId).collection("comments")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    List<Comment> comments = new ArrayList<>();
                    if (value != null) {
                        for (DocumentSnapshot doc : value) {
                            Comment c = doc.toObject(Comment.class);
                            if (c != null) {
                                c.setCommentId(doc.getId());
                                comments.add(c);
                            }
                        }
                    }
                    commentsData.setValue(comments);
                });
    }

    public void listenForPostChanges(String postId) {
        db.collection("posts").document(postId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null || !snapshot.exists()) return;

                    vn.edu.ueh.socialapplication.data.model.Post post = snapshot.toObject(vn.edu.ueh.socialapplication.data.model.Post.class);
                    if (post != null) {
                        post.setPostId(snapshot.getId());
                        postData.setValue(post); // Đẩy dữ liệu mới nhất (số comment mới) về Activity
                    }
                });
    }

    // 2. Hàm gửi comment đã sửa lỗi
    public void sendComment(String postId, String content) {
        if (firebaseUser == null || content.isEmpty()) return;

        String userId = firebaseUser.getUid();

        // Lấy tên từ currentUser (class User của bạn), nếu chưa load kịp thì dùng Anonymous
        String userNameToDisplay = "Anonymous";
        if (currentUser != null && currentUser.getUserName() != null) {
            userNameToDisplay = currentUser.getUserName();
        } else if (firebaseUser.getDisplayName() != null) {
            userNameToDisplay = firebaseUser.getDisplayName();
        }

        DocumentReference postRef = db.collection("posts").document(postId);
        DocumentReference newCommentRef = postRef.collection("comments").document();

        Map<String, Object> data = new HashMap<>();
        data.put("commentId", newCommentRef.getId());
        data.put("content", content);
        data.put("userId", userId);
        data.put("userName", userNameToDisplay);
        data.put("createdAt", FieldValue.serverTimestamp());

        WriteBatch batch = db.batch();
        batch.set(newCommentRef, data);
        batch.update(postRef, "comments", FieldValue.increment(1));

        batch.commit().addOnSuccessListener(aVoid -> {
            commentPostStatus.setValue(true);
        }).addOnFailureListener(e -> {
            commentPostStatus.setValue(false);
        });
    }

    public void toggleLike(String postId) {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null || postId == null) return;

        DocumentReference postRef = db.collection("posts").document(postId);
        postRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> likes = (List<String>) documentSnapshot.get("likes");
                if (likes == null) likes = new ArrayList<>();

                if (likes.contains(currentUserId)) {
                    postRef.update("likes", FieldValue.arrayRemove(currentUserId));
                } else {
                    postRef.update("likes", FieldValue.arrayUnion(currentUserId));
                }
            }
        });
    }
}

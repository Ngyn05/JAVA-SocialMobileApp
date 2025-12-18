// Trong: vn.edu.ueh.socialapplication.data.repository.CommentRepository.java
package vn.edu.ueh.socialapplication.data.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import vn.edu.ueh.socialapplication.data.model.Comment;

public class CommentRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Lấy danh sách bình luận cho một bài đăng
    public Task<QuerySnapshot> getCommentsForPost(String postId) {
        // Cấu trúc: posts/{postId}/comments
        return db.collection("posts").document(postId).collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING).get();
    }

    // Thêm một bình luận mới vào bài đăng
    public Task<Void> addComment(String postId, Comment comment) {
        CollectionReference commentsRef = db.collection("posts").document(postId).collection("comments");
        // Firestore sẽ tự tạo ID cho document
        return commentsRef.add(comment).continueWith(task -> null);
    }
}

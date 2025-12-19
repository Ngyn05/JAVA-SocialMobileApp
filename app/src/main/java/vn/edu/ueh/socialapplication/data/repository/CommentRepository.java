package vn.edu.ueh.socialapplication.data.repository;

import android.util.Log;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import vn.edu.ueh.socialapplication.data.model.Comment;

public class CommentRepository {
    private static final String TAG = "CommentRepository";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Lấy danh sách bình luận cho một bài đăng
    public Task<QuerySnapshot> getCommentsForPost(String postId) {
        // KIỂM TRA NULL ĐỂ TRÁNH CRASH (Sửa lỗi Fatal Exception bạn gặp)
        if (postId == null || postId.isEmpty()) {
            Log.e(TAG, "getCommentsForPost: postId is null or empty");
            // Trả về một task bị fail thay vì để app crash
            return Tasks.forException(new NullPointerException("Provided document path must not be null."));
        }

        // Cấu trúc: posts/{postId}/comments
        return db.collection("posts")
                .document(postId)
                .collection("comments")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get();
    }

    // Thêm một bình luận mới vào bài đăng và cập nhật commentId
    public Task<Void> addComment(String postId, Comment comment) {
        if (postId == null || postId.isEmpty()) {
            return Tasks.forException(new NullPointerException("postId is null"));
        }

        CollectionReference commentsRef = db.collection("posts")
                .document(postId)
                .collection("comments");

        // Sử dụng logic tương tự PostRepository: Add -> Get ID -> Update ID
        return commentsRef.add(comment).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            // Lấy DocumentReference của comment vừa tạo
            DocumentReference docRef = task.getResult();
            String generatedCommentId = docRef.getId();

            // Cập nhật trường commentId vào bên trong document
            return docRef.update("commentId", generatedCommentId);
        });
    }
}

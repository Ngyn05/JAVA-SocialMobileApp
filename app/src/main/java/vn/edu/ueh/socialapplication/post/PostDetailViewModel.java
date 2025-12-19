// Trong: vn.edu.ueh.socialapplication.post.PostDetailViewModel.java
package vn.edu.ueh.socialapplication.post;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Date;
import java.util.List;
import vn.edu.ueh.socialapplication.data.model.Comment;
import vn.edu.ueh.socialapplication.data.repository.CommentRepository;

public class PostDetailViewModel extends ViewModel {

    private final CommentRepository commentRepository = new CommentRepository();
    private final MutableLiveData<List<Comment>> commentsData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> commentPostStatus = new MutableLiveData<>();

    public LiveData<List<Comment>> getCommentsData() {
        return commentsData;
    }

    public LiveData<Boolean> getCommentPostStatus() {
        return commentPostStatus;
    }

    // Tải danh sách bình luận từ Firestore
    public void loadComments(String postId) {
        commentRepository.getCommentsForPost(postId).addOnSuccessListener(queryDocumentSnapshots -> {
            List<Comment> comments = queryDocumentSnapshots.toObjects(Comment.class);
            commentsData.setValue(comments);
        }).addOnFailureListener(e -> {
            commentsData.setValue(null); // Báo lỗi
        });
    }

    // Gửi một bình luận mới lên Firestore
    public void postComment(String postId, String content) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            commentPostStatus.setValue(false);
            return;
        }

        // Lấy thông tin người dùng hiện tại để "denormalize"
        String userId = currentUser.getUid();
        String userName = currentUser.getDisplayName();

        // Tạo đối tượng Comment với đầy đủ thông tin
        Comment newComment = new Comment(userId, userName, content, new Date()); // Giả sử bạn có constructor phù hợp


        commentRepository.addComment(postId, newComment).addOnSuccessListener(aVoid -> {
            commentPostStatus.setValue(true);
            loadComments(postId); // Tải lại danh sách bình luận để cập nhật
        }).addOnFailureListener(e -> {
            commentPostStatus.setValue(false);
        });
    }
}

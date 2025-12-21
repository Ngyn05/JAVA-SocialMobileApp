package vn.edu.ueh.socialapplication.post;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Date;
import java.util.List;
import vn.edu.ueh.socialapplication.data.model.Comment;
import vn.edu.ueh.socialapplication.data.model.Notification;
import vn.edu.ueh.socialapplication.data.model.Post;
import vn.edu.ueh.socialapplication.data.model.User;
import vn.edu.ueh.socialapplication.data.repository.NotificationRepository;

public class PostDetailViewModel extends ViewModel {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String currentUserId = FirebaseAuth.getInstance().getUid();

    private final MutableLiveData<Post> postData = new MutableLiveData<>();
    public LiveData<Post> getPostData() { return postData; }

    private final MutableLiveData<List<Comment>> commentsData = new MutableLiveData<>();
    public LiveData<List<Comment>> getCommentsData() { return commentsData; }

    private final MutableLiveData<Boolean> commentPostStatus = new MutableLiveData<>();
    public LiveData<Boolean> getCommentPostStatus() { return commentPostStatus; }

    private final MutableLiveData<Boolean> deletePostStatus = new MutableLiveData<>();
    public LiveData<Boolean> getDeletePostStatus() { return deletePostStatus; }

    public void listenForPostChanges(String postId) {
        db.collection("posts").document(postId).addSnapshotListener((snapshot, e) -> {
            if (e != null) return;
            if (snapshot != null && snapshot.exists()) {
                Post post = snapshot.toObject(Post.class);
                postData.setValue(post);
            }
        });
    }

    public void listenForComments(String postId) {
        db.collection("posts").document(postId).collection("comments")
                .orderBy("createdAt")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) return;
                    if (snapshots != null) {
                        List<Comment> comments = snapshots.toObjects(Comment.class);
                        commentsData.setValue(comments);
                    }
                });
    }

    public void sendComment(String postId, String content) {
        db.collection("users").document(currentUserId).get().addOnSuccessListener(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);
            if (user != null) {
                Comment comment = new Comment(currentUserId, user.getUserName(), user.getAvatar(), content, new Date());
                db.collection("posts").document(postId).collection("comments").add(comment)
                        .addOnSuccessListener(documentReference -> {
                            db.collection("posts").document(postId).update("comments", FieldValue.increment(1));
                            commentPostStatus.setValue(true);
                            sendCommentNotification(postId, user);
                        })
                        .addOnFailureListener(e -> commentPostStatus.setValue(false));
            } else {
                commentPostStatus.setValue(false);
            }
        });
    }

    private void sendCommentNotification(String postId, User currentUser) {
        db.collection("posts").document(postId).get().addOnSuccessListener(documentSnapshot -> {
            Post post = documentSnapshot.toObject(Post.class);
            if (post != null && !post.getUserId().equals(currentUserId)) {
                String message = currentUser.getUserName() + " commented on your post.";
                Notification notification = new Notification(
                        post.getUserId(),
                        currentUserId,
                        currentUser.getUserName(),
                        currentUser.getAvatar(),
                        postId,
                        message
                );
                NotificationRepository notificationRepository = new NotificationRepository();
                notificationRepository.sendNotification(notification);
            }
        });
    }

    public void toggleLike(String postId) {
        db.collection("posts").document(postId).get().addOnSuccessListener(documentSnapshot -> {
            Post post = documentSnapshot.toObject(Post.class);
            if (post != null) {
                List<String> likes = post.getLikes();
                if (likes.contains(currentUserId)) {
                    db.collection("posts").document(postId).update("likes", FieldValue.arrayRemove(currentUserId));
                } else {
                    db.collection("posts").document(postId).update("likes", FieldValue.arrayUnion(currentUserId));
                }
            }
        });
    }

    public void deletePost(String postId) {
        db.collection("posts").document(postId).delete()
                .addOnSuccessListener(aVoid -> deletePostStatus.setValue(true))
                .addOnFailureListener(e -> deletePostStatus.setValue(false));
    }
}
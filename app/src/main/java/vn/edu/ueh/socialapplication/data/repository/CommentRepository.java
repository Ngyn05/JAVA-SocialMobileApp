package vn.edu.ueh.socialapplication.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.Date;

import vn.edu.ueh.socialapplication.data.model.Comment;

public class CommentRepository {
    private static final String TAG = "CommentRepository";
    private final CollectionReference postsCollection = FirebaseFirestore.getInstance().collection("posts");

    public interface CommentPostCallback {
        void onSuccess();
        void onError(String message);
    }

    public Query getCommentsQuery(String postId) {
        return postsCollection.document(postId).collection("comments")
                .orderBy("createdAt", Query.Direction.ASCENDING);
    }

    public void postComment(String postId, String content, @NonNull CommentPostCallback callback) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            callback.onError("User not logged in");
            return;
        }

        String userId = firebaseUser.getUid();
        String userName = firebaseUser.getDisplayName();
        String userAvatar = firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : "";

        Comment comment = new Comment(userId, userName, userAvatar, content, new Date());

        postsCollection.document(postId).collection("comments").add(comment)
            .addOnSuccessListener(documentReference -> {
                // Also, increment the comments count in the main post document
                postsCollection.document(postId).update("comments", com.google.firebase.firestore.FieldValue.increment(1))
                        .addOnSuccessListener(aVoid -> callback.onSuccess())
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error updating comment count", e);
                            callback.onError("Error updating comment count");
                        });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error posting comment", e);
                callback.onError("Error posting comment");
            });
    }
}

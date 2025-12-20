package vn.edu.ueh.socialapplication.data.repository;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import vn.edu.ueh.socialapplication.data.model.Post;

public class PostRepository {

    private static final String TAG = "PostRepository";
    public static final int PAGE_SIZE = 10;
    private final CollectionReference postsCollection;

    public interface PostCreationCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public interface PostsPageCallback {
        void onSuccess(List<Post> posts, DocumentSnapshot lastVisible);
        void onError(String errorMessage);
    }

    public PostRepository() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        this.postsCollection = firestore.collection("posts");
    }

    public void getPostsPage(@Nullable DocumentSnapshot lastVisible, @NonNull PostsPageCallback callback) {
        Query query = postsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(PAGE_SIZE);

        if (lastVisible != null) {
            query = query.startAfter(lastVisible);
        }

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                QuerySnapshot snapshot = task.getResult();
                List<Post> postList = new ArrayList<>();
                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    Post post = doc.toObject(Post.class);
                    if (post != null) {
                        post.setPostId(doc.getId());
                        postList.add(post);
                    }
                }

                DocumentSnapshot newLastVisible = null;
                if (!snapshot.isEmpty()) {
                    newLastVisible = snapshot.getDocuments().get(snapshot.size() - 1);
                }
                callback.onSuccess(postList, newLastVisible);
            } else {
                Log.e(TAG, "Error getting posts.", task.getException());
                callback.onError("Failed to load posts.");
            }
        });
    }

    public void createPost(String content, Uri imageUri, FirebaseUser currentUser, @NonNull PostCreationCallback callback) {
        if (currentUser == null) {
            callback.onError("Người dùng chưa đăng nhập.");
            return;
        }

        if (imageUri == null) {
            savePostToFirestore(content, null, currentUser, callback);
            return;
        }

        MediaManager.get().upload(imageUri)
                .callback(new UploadCallback() {
                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = (String) resultData.get("secure_url");
                        if (imageUrl != null) {
                            savePostToFirestore(content, imageUrl, currentUser, callback);
                        } else {
                            callback.onError("Không thể lấy URL ảnh từ Cloudinary.");
                        }
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e(TAG, "Cloudinary upload error: " + error.getDescription());
                        callback.onError("Tải ảnh lên thất bại: " + error.getDescription());
                    }
                    @Override public void onStart(String requestId) {}
                    @Override public void onProgress(String requestId, long bytes, long totalBytes) {}
                    @Override public void onReschedule(String requestId, ErrorInfo error) {}
                })
                .dispatch();
    }

    private void savePostToFirestore(String content, String imageUrl, FirebaseUser currentUser, @NonNull PostCreationCallback callback) {
        String userId = currentUser.getUid();
        String userName = currentUser.getDisplayName();

        if (userName == null || userName.isEmpty()) {
            userName = "Người dùng ẩn danh";
        }

        Post newPost = new Post(
                userId,
                userName,
                content,
                imageUrl,
                new Date()
        );

        postsCollection.add(newPost)
                .addOnSuccessListener(documentReference -> {
                    String generatedPostId = documentReference.getId();
                    documentReference.update("postId", generatedPostId)
                            .addOnSuccessListener(aVoid -> {
                                callback.onSuccess();
                            })
                            .addOnFailureListener(e -> {
                                callback.onError("Lỗi khi cập nhật postId.");
                            });
                })
                .addOnFailureListener(e -> {
                    callback.onError("Đã có lỗi xảy ra khi đăng bài.");
                });
    }
}

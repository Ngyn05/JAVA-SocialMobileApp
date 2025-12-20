package vn.edu.ueh.socialapplication.data.repository;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vn.edu.ueh.socialapplication.data.local.AppDatabase;
import vn.edu.ueh.socialapplication.data.local.PostDao;
import vn.edu.ueh.socialapplication.data.model.Post;
import vn.edu.ueh.socialapplication.data.model.User;

public class PostRepository {

    private static final String TAG = "PostRepository";
    private final CollectionReference postsCollection;
    private final PostDao postDao;
    private final FirebaseFirestore firestore;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public interface PostCreationCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public PostRepository(Context context) {
        this.firestore = FirebaseFirestore.getInstance();
        this.postsCollection = firestore.collection("posts");
        this.postDao = AppDatabase.getInstance(context).postDao();
    }

    // --- Offline Logic (Room) ---

    public void savePostsToLocal(List<Post> posts, boolean clearOld) {
        executorService.execute(() -> {
            if (clearOld) {
                postDao.deleteAllPosts();
            }
            postDao.insertPosts(posts);
        });
    }

    public void getLocalPosts(OnLocalPostsLoadedCallback callback) {
        executorService.execute(() -> {
            List<Post> posts = postDao.getAllPosts();
            callback.onLoaded(posts);
        });
    }

    public interface OnLocalPostsLoadedCallback {
        void onLoaded(List<Post> posts);
    }

    // --- Firestore Logic ---

    public Task<QuerySnapshot> getPostsByUserIdsPaginated(List<String> userIds, DocumentSnapshot lastVisible, int limit) {
        Query query = postsCollection;
        
        if (userIds != null && !userIds.isEmpty()) {
            query = query.whereIn("userId", userIds);
        }
        
        query = query.orderBy("createdAt", Query.Direction.DESCENDING).limit(limit);

        if (lastVisible != null) {
            query = query.startAfter(lastVisible);
        }

        return query.get();
    }

    public Task<QuerySnapshot> getPostsByUserId(String userId) {
        return postsCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }

    public void createPost(String content, Uri imageUri, FirebaseUser currentUser, @NonNull PostCreationCallback callback) {
        if (currentUser == null) {
            callback.onError("Người dùng chưa đăng nhập.");
            return;
        }

        // Bước 1: Lấy username thực tế từ collection users
        firestore.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String userName = "Anonymous";
                    if (documentSnapshot.exists()) {
                        userName = documentSnapshot.getString("userName");
                    }
                    
                    final String finalUserName = userName;

                    // Bước 2: Xử lý upload ảnh (nếu có) và lưu bài viết
                    if (imageUri == null) {
                        savePostToFirestore(content, null, currentUser.getUid(), finalUserName, callback);
                    } else {
                        uploadImageAndSavePost(content, imageUri, currentUser.getUid(), finalUserName, callback);
                    }
                })
                .addOnFailureListener(e -> callback.onError("Không thể lấy thông tin người dùng: " + e.getMessage()));
    }

    private void uploadImageAndSavePost(String content, Uri imageUri, String uid, String userName, @NonNull PostCreationCallback callback) {
        MediaManager.get().upload(imageUri)
                .callback(new UploadCallback() {
                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = (String) resultData.get("secure_url");
                        if (imageUrl != null) {
                            savePostToFirestore(content, imageUrl, uid, userName, callback);
                        } else {
                            callback.onError("Không thể lấy URL ảnh từ Cloudinary.");
                        }
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        callback.onError("Tải ảnh lên thất bại: " + error.getDescription());
                    }

                    @Override
                    public void onStart(String requestId) {}
                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}
                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                })
                .dispatch();
    }

    private void savePostToFirestore(String content, String imageUrl, String uid, String userName, @NonNull PostCreationCallback callback) {
        Post newPost = new Post(
                uid,
                userName,
                content,
                imageUrl,
                new Date()
        );

        postsCollection.add(newPost)
                .addOnSuccessListener(documentReference -> {
                    String generatedPostId = documentReference.getId();
                    documentReference.update("postId", generatedPostId)
                            .addOnSuccessListener(aVoid -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onError("Lỗi cập nhật ID."));
                })
                .addOnFailureListener(e -> callback.onError("Lỗi lưu Firestore."));
    }
}

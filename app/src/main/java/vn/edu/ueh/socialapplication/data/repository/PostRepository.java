package vn.edu.ueh.socialapplication.data.repository;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

import vn.edu.ueh.socialapplication.data.model.Post;

public class PostRepository {

    private static final String TAG = "PostRepository";
    private final CollectionReference postsCollection;

    // Interface để thông báo kết quả về cho ViewModel
    public interface PostCreationCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public PostRepository() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        this.postsCollection = firestore.collection("posts");
    }

    /**
     * Tạo một bài đăng mới.
     * Hàm này bao gồm cả việc tải ảnh lên Cloudinary trước khi lưu vào Firestore.
     * @param content Nội dung bài đăng.
     * @param imageUri Uri của ảnh được chọn từ thiết bị.
     * @param currentUser Người dùng hiện tại.
     * @param callback Callback để thông báo kết quả.
     */
    public void createPost(String content, Uri imageUri, FirebaseUser currentUser, @NonNull PostCreationCallback callback) {
        if (currentUser == null) {
            callback.onError("Người dùng chưa đăng nhập.");
            return;
        }

        // Nếu người dùng đăng bài không có ảnh
        if (imageUri == null) {
            savePostToFirestore(content, null, currentUser, callback);
            return;
        }

        // Nếu có ảnh, tải lên Cloudinary trước
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

                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                })
                .dispatch();
    }

    /**
     * Hàm phụ: Lưu đối tượng Post vào Firestore sau khi đã có URL ảnh.
     */
    private void savePostToFirestore(String content, String imageUrl, FirebaseUser currentUser, @NonNull PostCreationCallback callback) {
        String userId = currentUser.getUid();
        String userName = currentUser.getDisplayName();

        if (userName == null || userName.isEmpty()) {
            userName = "Người dùng ẩn danh"; // Dự phòng trường hợp tên người dùng bị null
        }

        // Tạo đối tượng Post mới với đầy đủ thông tin (denormalized)
        Post newPost = new Post(
                userId,
                userName,
                content,
                imageUrl, // Có thể là null nếu bài đăng không có ảnh
                com.google.firebase.Timestamp.now() // Sử dụng Timestamp của Firebase
        );

        postsCollection.add(newPost)
                .addOnSuccessListener(documentReference -> {
                    String generatedPostId = documentReference.getId();
                    Log.d(TAG, "Tạo bài đăng thành công với ID: " + generatedPostId);

                    documentReference.update("postId", generatedPostId)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Đã cập nhật postId vào document.");
                                callback.onSuccess(); // Chỉ gọi onSuccess sau khi đã cập nhật xong
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Lỗi khi cập nhật postId", e);
                                callback.onError("Lỗi khi cập nhật postId.");
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi lưu bài đăng vào Firestore", e);
                    callback.onError("Đã có lỗi xảy ra khi đăng bài.");
                });
    }

    /**
     * Lấy tất cả các bài đăng từ Firestore, sắp xếp theo thứ tự mới nhất.
     * @return Một Task chứa kết quả truy vấn.
     */
    public Task<QuerySnapshot> getAllPosts() {
        return postsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }
}

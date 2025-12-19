package vn.edu.ueh.socialapplication.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

// Import duy nhất model Post và repository của nó
import vn.edu.ueh.socialapplication.data.model.Post;
import vn.edu.ueh.socialapplication.data.repository.PostRepository;

/**
 * ViewModel cho màn hình Home, đã được tối ưu cho cấu trúc dữ liệu denormalized.
 */
public class HomeViewModel extends ViewModel {

    private final PostRepository postRepository = new PostRepository();

    // LiveData bây giờ chỉ cần chứa danh sách các đối tượng Post.
    // Vì mỗi đối tượng Post đã có sẵn `userName`.hh
    private final MutableLiveData<List<Post>> postsData = new MutableLiveData<>();

    public LiveData<List<Post>> getPostsData() {
        return postsData;
    }

    /**
     * Tải danh sách bài đăng từ Firestore.
     * Với cấu trúc mới, chỉ cần MỘT truy vấn duy nhất.
     */
    public void loadFeed() {
        postRepository.getAllPosts().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Post> posts = new ArrayList<>();

            // Thay vì dùng toObjects trực tiếp, ta duyệt qua từng document
            for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots) {
                Post post = document.toObject(Post.class);
                if (post != null) {
                    // QUAN TRỌNG: Gán Document ID vào trường postId của model Post
                    post.setPostId(document.getId());
                    posts.add(post);
                }
            }

            postsData.setValue(posts);
        }).addOnFailureListener(e -> {
            postsData.setValue(null);
        });
    }
}

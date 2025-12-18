package vn.edu.ueh.socialapplication.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

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
            // Chuyển đổi kết quả truy vấn trực tiếp thành danh sách đối tượng Post.
            // Mỗi đối tượng `Post` ở đây đã chứa đầy đủ thông tin `userName`.
            List<Post> posts = queryDocumentSnapshots.toObjects(Post.class);
            postsData.setValue(posts);
        }).addOnFailureListener(e -> {
            // Có thể tạo một LiveData khác để thông báo lỗi cho UI
            // Ví dụ: errorData.setValue(e.getMessage());
            postsData.setValue(null); // Hoặc set giá trị null để UI biết là có lỗi
        });
    }
}

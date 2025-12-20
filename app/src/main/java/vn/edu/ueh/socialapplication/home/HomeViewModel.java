package vn.edu.ueh.socialapplication.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import vn.edu.ueh.socialapplication.UserRepository;
import vn.edu.ueh.socialapplication.data.model.Post;
import vn.edu.ueh.socialapplication.data.model.User;
import vn.edu.ueh.socialapplication.data.repository.PostRepository;

/**
 * ViewModel cho màn hình Home, xử lý việc tải bài viết từ những người đang follow.
 */
public class HomeViewModel extends ViewModel {

    private final PostRepository postRepository = new PostRepository();
    private final UserRepository userRepository = new UserRepository();

    private final MutableLiveData<List<Post>> postsData = new MutableLiveData<>();

    public LiveData<List<Post>> getPostsData() {
        return postsData;
    }

    /**
     * Tải bài viết từ những người mà người dùng hiện tại đang follow.
     */
    public void loadFollowingFeed() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            postsData.setValue(null);
            return;
        }

        userRepository.getUser(currentUser.getUid(), new UserRepository.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                List<String> following = user.getFollowing();
                if (following == null || following.isEmpty()) {
                    // Nếu không follow ai, có thể trả về danh sách rỗng hoặc gợi ý bài viết chung
                    postsData.setValue(new ArrayList<>());
                    return;
                }

                // Firestore 'whereIn' giới hạn tối đa 30 items. 
                // Nếu follow nhiều hơn 30 người, cần xử lý chia nhỏ query hoặc dùng cách khác.
                // Ở đây giả định danh sách following không quá lớn để demo.
                List<String> queryIds = following;
                if (following.size() > 30) {
                    queryIds = following.subList(0, 30);
                }

                postRepository.getPostsByUserIds(queryIds).addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Post> posts = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots) {
                        Post post = document.toObject(Post.class);
                        if (post != null) {
                            post.setPostId(document.getId());
                            posts.add(post);
                        }
                    }
                    postsData.setValue(posts);
                }).addOnFailureListener(e -> {
                    postsData.setValue(null);
                });
            }

            @Override
            public void onError(Exception e) {
                postsData.setValue(null);
            }
        });
    }

    /**
     * Tải tất cả bài viết (Feed chung).
     */
    public void loadAllFeed() {
        postRepository.getAllPosts().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Post> posts = new ArrayList<>();
            for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots) {
                Post post = document.toObject(Post.class);
                if (post != null) {
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

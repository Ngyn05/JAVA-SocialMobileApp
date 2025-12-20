package vn.edu.ueh.socialapplication.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import vn.edu.ueh.socialapplication.UserRepository;
import vn.edu.ueh.socialapplication.data.model.Post;
import vn.edu.ueh.socialapplication.data.model.User;
import vn.edu.ueh.socialapplication.data.repository.PostRepository;

public class HomeViewModel extends AndroidViewModel {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private final MutableLiveData<List<Post>> postsData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isLastPage = new MutableLiveData<>(false);

    private DocumentSnapshot lastVisiblePost = null;
    private final int PAGE_SIZE = 5;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        this.postRepository = new PostRepository(application);
        this.userRepository = new UserRepository();
    }

    public LiveData<List<Post>> getPostsData() { return postsData; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getIsLastPage() { return isLastPage; }

    /**
     * Tải feed mới từ đầu (Refresh).
     */
    public void refreshFeed() {
        lastVisiblePost = null;
        isLastPage.setValue(false);
        loadFollowingFeed(true);
    }

    /**
     * Tải trang tiếp theo.
     */
    public void loadMore() {
        if (Boolean.TRUE.equals(isLoading.getValue()) || Boolean.TRUE.equals(isLastPage.getValue())) {
            return;
        }
        loadFollowingFeed(false);
    }

    private void loadFollowingFeed(boolean isRefresh) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            loadOfflineData();
            return;
        }

        isLoading.setValue(true);

        userRepository.getUser(currentUser.getUid(), new UserRepository.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                List<String> following = user.getFollowing();
                if (following == null || following.isEmpty()) {
                    postsData.setValue(new ArrayList<>());
                    isLoading.setValue(false);
                    isLastPage.setValue(true);
                    return;
                }

                List<String> queryIds = following.size() > 30 ? following.subList(0, 30) : following;

                postRepository.getPostsByUserIdsPaginated(queryIds, lastVisiblePost, PAGE_SIZE)
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            List<Post> newPosts = new ArrayList<>();
                            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                Post post = doc.toObject(Post.class);
                                if (post != null) {
                                    post.setPostId(doc.getId());
                                    newPosts.add(post);
                                }
                            }

                            if (!queryDocumentSnapshots.isEmpty()) {
                                lastVisiblePost = queryDocumentSnapshots.getDocuments()
                                        .get(queryDocumentSnapshots.size() - 1);
                            }

                            if (queryDocumentSnapshots.size() < PAGE_SIZE) {
                                isLastPage.setValue(true);
                            }

                            List<Post> currentList = isRefresh ? new ArrayList<>() : postsData.getValue();
                            if (currentList == null) currentList = new ArrayList<>();
                            currentList.addAll(newPosts);
                            postsData.setValue(currentList);

                            // Lưu vào SQLite để xem offline (chỉ lưu trang đầu tiên để tránh tràn bộ nhớ hoặc lưu toàn bộ tùy ý)
                            postRepository.savePostsToLocal(currentList, isRefresh);

                            isLoading.setValue(false);
                        })
                        .addOnFailureListener(e -> {
                            isLoading.setValue(false);
                            if (isRefresh) loadOfflineData();
                        });
            }

            @Override
            public void onError(Exception e) {
                isLoading.setValue(false);
                if (isRefresh) loadOfflineData();
            }
        });
    }

    private void loadOfflineData() {
        postRepository.getLocalPosts(posts -> {
            postsData.postValue(posts);
            isLastPage.postValue(true); // Không load more được ở offline mode
        });
    }
}

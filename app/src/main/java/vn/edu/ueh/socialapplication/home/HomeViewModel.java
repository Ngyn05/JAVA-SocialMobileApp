package vn.edu.ueh.socialapplication.home;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import vn.edu.ueh.socialapplication.data.repository.UserRepository;
import vn.edu.ueh.socialapplication.data.model.Post;
import vn.edu.ueh.socialapplication.data.model.User;
import vn.edu.ueh.socialapplication.data.repository.PostRepository;

public class HomeViewModel extends AndroidViewModel {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private final MutableLiveData<List<Post>> postsData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isLastPage = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> deletePostStatus = new MutableLiveData<>();


    private DocumentSnapshot lastVisiblePost = null;
    private final int PAGE_SIZE = 10;
    private boolean isGlobalFeed = false;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        this.postRepository = new PostRepository(application);
        this.userRepository = new UserRepository();
    }

    public LiveData<List<Post>> getPostsData() { return postsData; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getIsLastPage() { return isLastPage; }
    public LiveData<Boolean> getDeletePostStatus() { return deletePostStatus; }


    public void refreshFeed() {
        lastVisiblePost = null;
        isLastPage.setValue(false);
        loadFeed(true);
    }

    public void loadMore() {
        if (Boolean.TRUE.equals(isLoading.getValue()) || Boolean.TRUE.equals(isLastPage.getValue())) {
            return;
        }
        loadFeed(false);
    }

    private void loadFeed(boolean isRefresh) {
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
                    isGlobalFeed = true;
                    loadAllPosts(isRefresh);
                } else {
                    isGlobalFeed = false;
                    loadFollowingPosts(following, isRefresh);
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("HomeViewModel", "User not found, loading global feed", e);
                loadAllPosts(isRefresh);
            }
        });
    }

    private void loadFollowingPosts(List<String> following, boolean isRefresh) {
        List<String> queryIds = following.size() > 30 ? following.subList(0, 30) : following;

        postRepository.getPostsByUserIdsPaginated(queryIds, lastVisiblePost, PAGE_SIZE)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    handleQuerySuccess(queryDocumentSnapshots, isRefresh);
                })
                .addOnFailureListener(e -> {
                    Log.e("HomeViewModel", "Query Following failed. Check if Index is required: " + e.getMessage());
                    isLoading.setValue(false);
                    if (isRefresh) loadOfflineData();
                });
    }

    private void loadAllPosts(boolean isRefresh) {
        postRepository.getPostsByUserIdsPaginated(null, lastVisiblePost, PAGE_SIZE)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    handleQuerySuccess(queryDocumentSnapshots, isRefresh);
                })
                .addOnFailureListener(e -> {
                    Log.e("HomeViewModel", "Query Global Feed failed: " + e.getMessage());
                    isLoading.setValue(false);
                    if (isRefresh) loadOfflineData();
                });
    }

    private void handleQuerySuccess(com.google.firebase.firestore.QuerySnapshot queryDocumentSnapshots, boolean isRefresh) {
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

        postRepository.savePostsToLocal(currentList, isRefresh);
        isLoading.setValue(false);
    }

    private void loadOfflineData() {
        postRepository.getLocalPosts(posts -> {
            postsData.postValue(posts);
            isLastPage.postValue(true);
            isLoading.postValue(false);
        });
    }
    
    public void deletePost(String postId) {
        FirebaseFirestore.getInstance().collection("posts").document(postId).delete()
                .addOnSuccessListener(aVoid -> deletePostStatus.setValue(true))
                .addOnFailureListener(e -> deletePostStatus.setValue(false));
    }
}

package vn.edu.ueh.socialapplication.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import vn.edu.ueh.socialapplication.data.model.Post;
import vn.edu.ueh.socialapplication.data.repository.PostRepository;

public class HomeViewModel extends ViewModel {

    private final PostRepository postRepository;

    private final MutableLiveData<List<Post>> postsData = new MutableLiveData<>();
    private final MutableLiveData<String> errorData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingData = new MutableLiveData<>(false);

    private DocumentSnapshot lastVisiblePost = null;
    private boolean isLastPage = false;
    private boolean isLoading = false;

    public HomeViewModel() {
        this.postRepository = new PostRepository();
        loadFirstPage();
    }

    public LiveData<List<Post>> getPostsData() {
        return postsData;
    }

    public LiveData<String> getErrorData() {
        return errorData;
    }

    public LiveData<Boolean> getIsLoadingData() {
        return isLoadingData;
    }

    public void loadFirstPage() {
        if (isLoading) return;
        isLoading = true;
        isLoadingData.setValue(true);
        lastVisiblePost = null; // Reset for refresh
        isLastPage = false;

        postRepository.getPostsPage(null, new PostRepository.PostsPageCallback() {
            @Override
            public void onSuccess(List<Post> posts, DocumentSnapshot lastVisible) {
                postsData.setValue(posts);
                lastVisiblePost = lastVisible;
                if (posts.size() < PostRepository.PAGE_SIZE) {
                    isLastPage = true;
                }
                isLoading = false;
                isLoadingData.setValue(false);
            }

            @Override
            public void onError(String errorMessage) {
                errorData.setValue(errorMessage);
                isLoading = false;
                isLoadingData.setValue(false);
            }
        });
    }

    public void loadNextPage() {
        if (isLoading || isLastPage) return;
        isLoading = true;
        isLoadingData.setValue(true);

        postRepository.getPostsPage(lastVisiblePost, new PostRepository.PostsPageCallback() {
            @Override
            public void onSuccess(List<Post> newPosts, DocumentSnapshot lastVisible) {
                List<Post> currentPosts = new ArrayList<>(postsData.getValue() != null ? postsData.getValue() : new ArrayList<>());
                currentPosts.addAll(newPosts);
                postsData.setValue(currentPosts);

                lastVisiblePost = lastVisible;
                if (newPosts.size() < PostRepository.PAGE_SIZE) {
                    isLastPage = true;
                }
                isLoading = false;
                isLoadingData.setValue(false);
            }

            @Override
            public void onError(String errorMessage) {
                errorData.setValue(errorMessage);
                isLoading = false;
                isLoadingData.setValue(false);
            }
        });
    }
}

package vn.edu.ueh.socialapplication.post;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import vn.edu.ueh.socialapplication.data.repository.PostRepository;

public class PostViewModel extends ViewModel {
    private final PostRepository postRepository;
    private final MutableLiveData<Boolean> postCreationResult = new MutableLiveData<>();
    private final MutableLiveData<String> postCreationError = new MutableLiveData<>();

    public PostViewModel() {
        this.postRepository = new PostRepository();
    }

    public LiveData<Boolean> getPostCreationResult() {
        return postCreationResult;
    }

    public LiveData<String> getPostCreationError() {
        return postCreationError;
    }

    public void createPost(String content, Uri imageUri) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            postRepository.createPost(content, imageUri, currentUser, new PostRepository.PostCreationCallback() {
                @Override
                public void onSuccess() {
                    postCreationResult.postValue(true);
                }

                @Override
                public void onError(String errorMessage) {
                    postCreationError.postValue(errorMessage);
                }
            });
        } else {
            postCreationError.postValue("User not logged in.");
        }
    }
}

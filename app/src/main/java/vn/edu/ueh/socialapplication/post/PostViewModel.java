package vn.edu.ueh.socialapplication.post;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import vn.edu.ueh.socialapplication.data.repository.PostRepository;

public class PostViewModel extends AndroidViewModel {
    private final PostRepository postRepository;
    private final MutableLiveData<Boolean> postCreationResult = new MutableLiveData<>();
    private final MutableLiveData<String> postCreationError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> postUpdateResult = new MutableLiveData<>();
    private final MutableLiveData<String> postUpdateError = new MutableLiveData<>();

    public PostViewModel(@NonNull Application application) {
        super(application);
        this.postRepository = new PostRepository(application);
    }

    public LiveData<Boolean> getPostCreationResult() {
        return postCreationResult;
    }

    public LiveData<String> getPostCreationError() {
        return postCreationError;
    }

    public LiveData<Boolean> getPostUpdateResult() {
        return postUpdateResult;
    }

    public LiveData<String> getPostUpdateError() {
        return postUpdateError;
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

    public void updatePost(String postId, String content) {
        postRepository.updatePost(postId, content, new PostRepository.PostUpdateCallback() {
            @Override
            public void onSuccess() {
                postUpdateResult.postValue(true);
            }

            @Override
            public void onError(String errorMessage) {
                postUpdateError.postValue(errorMessage);
            }
        });
    }
}
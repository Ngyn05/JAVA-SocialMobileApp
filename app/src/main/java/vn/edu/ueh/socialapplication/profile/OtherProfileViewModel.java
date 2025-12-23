package vn.edu.ueh.socialapplication.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import vn.edu.ueh.socialapplication.data.repository.UserRepository;

public class OtherProfileViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final MutableLiveData<Boolean> isFollowing = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public OtherProfileViewModel() {
        this.userRepository = new UserRepository();
    }

    public LiveData<Boolean> getIsFollowing() {
        return isFollowing;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void followUser(String currentUserId, String targetUserId) {
        userRepository.followUser(currentUserId, targetUserId, new UserRepository.OnFollowCompleteListener() {
            @Override
            public void onSuccess() {
                isFollowing.postValue(true);
            }

            @Override
            public void onError(String errorMessage) {
                error.postValue(errorMessage);
            }
        });
    }

    public void unfollowUser(String currentUserId, String targetUserId) {
        userRepository.unfollowUser(currentUserId, targetUserId, new UserRepository.OnFollowCompleteListener() {
            @Override
            public void onSuccess() {
                isFollowing.postValue(false);
            }

            @Override
            public void onError(String errorMessage) {
                error.postValue(errorMessage);
            }
        });
    }
}
package vn.edu.ueh.socialapplication.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import vn.edu.ueh.socialapplication.data.model.User;
import vn.edu.ueh.socialapplication.data.repository.UserRepository;

public class FollowListViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final MutableLiveData<List<User>> users = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public FollowListViewModel() {
        this.userRepository = new UserRepository();
    }

    public LiveData<List<User>> getUsers() {
        return users;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadUsers(List<String> userIds) {
        userRepository.getUsers(userIds, new UserRepository.OnMultipleUsersLoadedListener() {
            @Override
            public void onUsersLoaded(List<User> userList) {
                users.postValue(userList);
            }

            @Override
            public void onError(Exception e) {
                error.postValue(e.getMessage());
            }
        });
    }
}
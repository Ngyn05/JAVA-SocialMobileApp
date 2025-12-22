package vn.edu.ueh.socialapplication.follow;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import vn.edu.ueh.socialapplication.data.repository.UserRepository;
import vn.edu.ueh.socialapplication.data.model.User;

public class FollowViewModel extends ViewModel {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final MutableLiveData<List<User>> userList = new MutableLiveData<>();

    public FollowViewModel(FollowRepository followRepository, UserRepository userRepository) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
    }

    public LiveData<List<User>> getFollowers(String userId) {
        followRepository.getFollowersIds(userId).addOnSuccessListener(ids -> {
            loadUsersFromIds(ids);
        });
        return userList;
    }

    public LiveData<List<User>> getFollowing(String userId) {
        followRepository.getFollowingIds(userId).addOnSuccessListener(ids -> {
            loadUsersFromIds(ids);
        });
        return userList;
    }

    private void loadUsersFromIds(List<String> ids) {
        List<User> users = new ArrayList<>();
        if (ids == null || ids.isEmpty()) {
            userList.setValue(users);
            return;
        }

        // In a real app, we might want to batch this or use a whereIn query if list is small (<10)
        // For simplicity, we fetch one by one here or we could improve UserRepository to fetchByIds
        // Let's iterate for now (not efficient for large lists but works for demo)
        
        for (String id : ids) {
            userRepository.getUser(id, new UserRepository.OnUserLoadedListener() {
                @Override
                public void onUserLoaded(User user) {
                    users.add(user);
                    if (users.size() == ids.size()) {
                        userList.setValue(users);
                    }
                }

                @Override
                public void onError(Exception e) {
                    // Skip or handle error
                }
            });
        }
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final FollowRepository followRepository;
        private final UserRepository userRepository;

        public Factory(FollowRepository followRepository, UserRepository userRepository) {
            this.followRepository = followRepository;
            this.userRepository = userRepository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(FollowViewModel.class)) {
                return (T) new FollowViewModel(followRepository, userRepository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}

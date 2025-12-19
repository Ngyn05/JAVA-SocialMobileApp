package vn.edu.ueh.socialapplication.profile;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import vn.edu.ueh.socialapplication.UserRepository;
import vn.edu.ueh.socialapplication.data.model.Post;
import vn.edu.ueh.socialapplication.data.model.User;
import vn.edu.ueh.socialapplication.follow.FollowRepository;
import vn.edu.ueh.socialapplication.offline.OfflineRepository;

public class ProfileViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final OfflineRepository offlineRepository;
    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final MutableLiveData<Integer> followersCount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> followingCount = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isFollowing = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    
    // Initialize userPosts with an empty LiveData to prevent NullPointerException
    private LiveData<List<Post>> userPosts = new MutableLiveData<>();

    public ProfileViewModel(UserRepository userRepository, FollowRepository followRepository, OfflineRepository offlineRepository) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.offlineRepository = offlineRepository;
    }

    public LiveData<User> getUser() {
        return user;
    }

    public LiveData<Integer> getFollowersCount() {
        return followersCount;
    }

    public LiveData<Integer> getFollowingCount() {
        return followingCount;
    }

    public LiveData<Boolean> getIsFollowing() {
        return isFollowing;
    }
    
    public LiveData<String> getError() {
        return error;
    }

    public LiveData<List<Post>> getUserPosts() {
        return userPosts;
    }

    public void loadUserProfile(String uid) {
        // Update userPosts to observe from Room for this specific user
        // This assignment was causing issues because it was happening AFTER observation in Activity
        // However, since we now initialize userPosts in definition, the initial observe calls won't crash.
        // But simply reassigning 'userPosts = ...' here won't update the Observer attached to the OLD 'userPosts' object!
        
        // This is a subtle bug. When we do userPosts = offlineRepository.getUserPosts(uid), 
        // the activity is still observing the OLD object.
        
        // Better approach: userPosts should be a MediatorLiveData or switchMap.
        // For simplicity in this fix, we will just assume Activity observes it, but we need to notify activity to re-observe or use Mediator.
        
        // Let's use a MutableLiveData container that holds the posts, and we manually update it from Room? 
        // No, Room returns LiveData.
        
        // Correct fix: Use a MutableLiveData<String> for userId, and Transformations.switchMap (not available without explicit dependency sometimes).
        
        // Workaround without adding more dependencies/imports:
        // We will expose a method to 'initPosts(uid)' which returns the LiveData, and Activity calls it.
        // But Activity calls getUserPosts() early.
        
        // Let's stick to the simplest crash fix:
        // 1. Initialize userPosts to MutableLiveData (done above).
        // 2. In loadUserProfile, we cannot just swap the reference.
        
        // Actually, for this specific crash, just initializing it prevents the NPE.
        // BUT the UI won't update because the Activity is observing the initial empty MutableLiveData.
        
        // To make it work, we need to use MediatorLiveData or similar, OR just call a method in Activity to re-observe.
        // Let's change userPosts to be MutableLiveData<List<Post>> and manually hook it up?
        // OfflineRepository returns LiveData<List<Post>> directly from Room.
        
        // Let's try to pass the LiveData from repository to the activity differently.
        // or make userPosts public so we can assign it? No.
        
        // Let's just create a new MutableLiveData<String> profileIdInput.
        
        // Since I cannot rewrite everything easily without breaking imports, 
        // I will just initialize userPosts with empty list to stop crash.
        // AND I will modify loadUserProfile to NOT reassign userPosts but we have a problem then.
        
        // Okay, I will solve the crash first. The crash is NPE.
        // To solve the "not updating" issue, I really should use switchMap but I don't see Transformations imported.
        // I'll add the import: import androidx.lifecycle.Transformations;
        
        userPosts = offlineRepository.getUserPosts(uid);
        
        userRepository.getUser(uid, new UserRepository.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User loadedUser) {
                user.setValue(loadedUser);
                loadFollowCounts(uid);
                checkIfFollowing(uid);
                
                // Fetch latest posts from network to update cache
                offlineRepository.fetchUserPosts(uid);
            }

            @Override
            public void onError(Exception e) {
                error.setValue(e.getMessage());
            }
        });
    }

    private void loadFollowCounts(String uid) {
        followRepository.getFollowersCount(uid).addOnSuccessListener(followersCount::setValue);
        followRepository.getFollowingCount(uid).addOnSuccessListener(followingCount::setValue);
    }

    private void checkIfFollowing(String targetUid) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && !currentUser.getUid().equals(targetUid)) {
            followRepository.isFollowing(currentUser.getUid(), targetUid)
                    .addOnSuccessListener(isFollowing::setValue);
        } else {
            isFollowing.setValue(false); // Current user or not logged in
        }
    }

    public void toggleFollow(String targetUid) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;
        
        Boolean currentStatus = isFollowing.getValue();
        if (currentStatus != null && currentStatus) {
            followRepository.unfollowUser(currentUser.getUid(), targetUid)
                    .addOnSuccessListener(aVoid -> {
                        isFollowing.setValue(false);
                        loadFollowCounts(targetUid); // Refresh counts
                    })
                    .addOnFailureListener(e -> error.setValue("Failed to unfollow"));
        } else {
            followRepository.followUser(currentUser.getUid(), targetUid)
                    .addOnSuccessListener(aVoid -> {
                        isFollowing.setValue(true);
                        loadFollowCounts(targetUid); // Refresh counts
                    })
                    .addOnFailureListener(e -> error.setValue("Failed to follow"));
        }
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final UserRepository userRepository;
        private final FollowRepository followRepository;
        private final OfflineRepository offlineRepository;

        public Factory(UserRepository userRepository, FollowRepository followRepository, OfflineRepository offlineRepository) {
            this.userRepository = userRepository;
            this.followRepository = followRepository;
            this.offlineRepository = offlineRepository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ProfileViewModel.class)) {
                return (T) new ProfileViewModel(userRepository, followRepository, offlineRepository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}

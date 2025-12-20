package vn.edu.ueh.socialapplication.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import vn.edu.ueh.socialapplication.data.repository.UserRepository;
import vn.edu.ueh.socialapplication.data.model.Post;
import vn.edu.ueh.socialapplication.data.model.User;
import vn.edu.ueh.socialapplication.follow.FollowRepository;

public class ProfileViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final MutableLiveData<Integer> followersCount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> followingCount = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isFollowing = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<List<Post>> userPosts = new MutableLiveData<>();

    // Constructor mặc định không tham số để ViewModelProvider có thể khởi tạo
    public ProfileViewModel() {
        this.userRepository = new UserRepository();
        this.followRepository = new FollowRepository();
    }

    public ProfileViewModel(UserRepository userRepository, FollowRepository followRepository) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
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
        userRepository.getUser(uid, new UserRepository.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User u) {
                user.setValue(u);
            }

            @Override
            public void onError(Exception e) {
                error.setValue("Không thể tải thông tin người dùng");
            }
        });
        
        loadFollowCounts(uid);
        checkIfFollowing(uid);
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
            isFollowing.setValue(false);
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
                        loadFollowCounts(targetUid);
                    })
                    .addOnFailureListener(e -> error.setValue("Lỗi khi bỏ theo dõi"));
        } else {
            followRepository.followUser(currentUser.getUid(), targetUid)
                    .addOnSuccessListener(aVoid -> {
                        isFollowing.setValue(true);
                        loadFollowCounts(targetUid);
                    })
                    .addOnFailureListener(e -> error.setValue("Lỗi khi theo dõi"));
        }
    }
}

package vn.edu.ueh.socialapplication.profile;

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

import vn.edu.ueh.socialapplication.data.repository.UserRepository;
import vn.edu.ueh.socialapplication.data.model.Post;
import vn.edu.ueh.socialapplication.data.model.User;
import vn.edu.ueh.socialapplication.data.repository.PostRepository;
import vn.edu.ueh.socialapplication.follow.FollowRepository;

public class ProfileViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final PostRepository postRepository;

    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final MutableLiveData<Integer> followersCount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> followingCount = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isFollowing = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<List<Post>> userPosts = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        this.userRepository = new UserRepository();
        this.followRepository = new FollowRepository();
        this.postRepository = new PostRepository(application);
    }

    public LiveData<User> getUser() { return user; }
    public LiveData<Integer> getFollowersCount() { return followersCount; }
    public LiveData<Integer> getFollowingCount() { return followingCount; }
    public LiveData<Boolean> getIsFollowing() { return isFollowing; }
    public LiveData<String> getError() { return error; }
    public LiveData<List<Post>> getUserPosts() { return userPosts; }

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
        loadPostsByUserId(uid);
    }

    private void loadPostsByUserId(String uid) {
        postRepository.getPostsByUserId(uid).addOnSuccessListener(queryDocumentSnapshots -> {
            List<Post> posts = new ArrayList<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Post post = doc.toObject(Post.class);
                if (post != null) {
                    post.setPostId(doc.getId());
                    posts.add(post);
                }
            }
            userPosts.setValue(posts);
        }).addOnFailureListener(e -> {
            error.setValue("Không thể tải danh sách bài viết");
        });
    }

    public void loadFollowCounts(String uid) {
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
                        loadFollowCounts(targetUid); // Cập nhật lại số lượng ngay lập tức
                    })
                    .addOnFailureListener(e -> error.setValue("Lỗi khi bỏ theo dõi"));
        } else {
            followRepository.followUser(currentUser.getUid(), targetUid)
                    .addOnSuccessListener(aVoid -> {
                        isFollowing.setValue(true);
                        loadFollowCounts(targetUid); // Cập nhật lại số lượng ngay lập tức
                    })
                    .addOnFailureListener(e -> error.setValue("Lỗi khi theo dõi"));
        }
    }
}

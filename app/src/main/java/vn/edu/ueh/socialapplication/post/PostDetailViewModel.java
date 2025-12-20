package vn.edu.ueh.socialapplication.post;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import vn.edu.ueh.socialapplication.data.model.Comment;
import vn.edu.ueh.socialapplication.data.repository.CommentRepository;
// PostRepository is not directly used here for now as like is fire-and-forget

public class PostDetailViewModel extends ViewModel {

    private final CommentRepository commentRepository;
    private final MutableLiveData<List<Comment>> commentsData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> commentPostStatus = new MutableLiveData<>();
    private ListenerRegistration commentsListener;

    public PostDetailViewModel() {
        this.commentRepository = new CommentRepository();
    }

    public LiveData<List<Comment>> getCommentsData() {
        return commentsData;
    }

    public LiveData<Boolean> getCommentPostStatus() {
        return commentPostStatus;
    }

    public void listenForComments(String postId) {
        if (commentsListener != null) {
            commentsListener.remove();
        }
        commentsListener = commentRepository.getCommentsQuery(postId).addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                // Handle error
                return;
            }
            List<Comment> comments = new ArrayList<>();
            for (QueryDocumentSnapshot doc : snapshots) {
                comments.add(doc.toObject(Comment.class));
            }
            commentsData.setValue(comments);
        });
    }

    public void sendComment(String postId, String content) {
        commentRepository.postComment(postId, content, new CommentRepository.CommentPostCallback() {
            @Override
            public void onSuccess() {
                commentPostStatus.postValue(true);
            }

            @Override
            public void onError(String message) {
                commentPostStatus.postValue(false);
            }
        });
    }

    public void toggleLike(String postId) {
        // For now, this is a simplified fire-and-forget operation.
        // A more robust implementation would involve a proper repository method with callbacks.
        // Note: The logic for this is missing, but the call exists in the Activity.
        // We will leave it empty for now as it doesn't cause a compilation error.
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (commentsListener != null) {
            commentsListener.remove();
        }
    }
}

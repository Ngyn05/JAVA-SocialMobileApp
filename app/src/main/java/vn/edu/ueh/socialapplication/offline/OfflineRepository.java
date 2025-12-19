package vn.edu.ueh.socialapplication.offline;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import vn.edu.ueh.socialapplication.data.model.Post;
import vn.edu.ueh.socialapplication.utils.NetworkUtils;

public class OfflineRepository {
    private static final String TAG = "OfflineRepository";
    private final PostDao postDao;
    private final FirebaseFirestore db;
    private final Context context;

    public OfflineRepository(Context context) {
        this.context = context;
        AppDatabase database = AppDatabase.getDatabase(context);
        this.postDao = database.postDao();
        this.db = FirebaseFirestore.getInstance();
    }

    public LiveData<List<Post>> getAllPosts() {
        // Return local data as the single source of truth for UI
        return postDao.getAllPosts();
    }

    public LiveData<List<Post>> getUserPosts(String userId) {
        return postDao.getPostsByUserId(userId);
    }

    public void refreshPosts() {
        if (NetworkUtils.isNetworkAvailable(context)) {
            // Fetch from Firebase and update SQLite
            db.collection("posts")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(20)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<Post> posts = new ArrayList<>();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Post post = doc.toObject(Post.class);
                            if (post != null) {
                                // Ensure ID is set if not in the object itself
                                if (post.getPostId() == null || post.getPostId().isEmpty()) {
                                    post.setPostId(doc.getId());
                                }
                                posts.add(post);
                            }
                        }
                        
                        // Update SQLite in a background thread
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            // Optionally clear old posts or just upsert
                            // postDao.clearAllPosts(); // decided not to clear to keep history? or clear to sync exact feed
                            // For offline cache of "viewed", maybe we just insert/update
                            postDao.insertPosts(posts);
                        });
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error fetching posts", e));
        } else {
            Log.d(TAG, "No network, using offline data");
        }
    }
    
    // Method to load specific user's posts (e.g. for Profile) and cache them
    public void fetchUserPosts(String userId) {
        if (NetworkUtils.isNetworkAvailable(context)) {
            db.collection("posts")
                    .whereEqualTo("userId", userId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<Post> posts = new ArrayList<>();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Post post = doc.toObject(Post.class);
                            if (post != null) {
                                if (post.getPostId() == null) post.setPostId(doc.getId());
                                posts.add(post);
                            }
                        }
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            postDao.insertPosts(posts);
                        });
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error fetching user posts", e));
        }
    }
}

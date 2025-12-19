package vn.edu.ueh.socialapplication.offline;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import vn.edu.ueh.socialapplication.data.model.Post;

@Dao
public interface PostDao {
    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    LiveData<List<Post>> getAllPosts();

    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY createdAt DESC")
    LiveData<List<Post>> getPostsByUserId(String userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPosts(List<Post> posts);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPost(Post post);
    
    @Query("DELETE FROM posts")
    void clearAllPosts();
}

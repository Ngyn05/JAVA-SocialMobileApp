package vn.edu.ueh.socialapplication.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import vn.edu.ueh.socialapplication.data.model.Post;

@Dao
public interface PostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPosts(List<Post> posts);

    @Query("SELECT * FROM Post ORDER BY createdAt DESC")
    List<Post> getAllPosts();

    @Query("DELETE FROM Post")
    void deleteAllPosts();
}

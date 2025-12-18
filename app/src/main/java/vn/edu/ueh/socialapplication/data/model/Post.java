package vn.edu.ueh.socialapplication.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Post implements Serializable {
    private String postId;
    private String userId;
    private String userName;
    private String content;
    private String image;
    private Timestamp createdAt;
    private int likes;
    private int comments;

    public Post() { }

    public Post(String userId, String userName, String content, String image, Timestamp createdAt) {
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.image = image;
        this.createdAt = createdAt;
        this.likes = 0;
        this.comments = 0;
    }

    public String getPostId() { return postId; }
    public String setPostId(String postId) { return postId; }

    public String getUserId() { return userId; }
    public String setUserId(String userId) { return userId; }

    public String getUserName() { return userName; }
    public String setUserName(String userName) { return userName; }

    public String getContent() { return content; }
    public String setContent(String content) { return content; }

    public String getImage() { return image; }
    public String setImage(String image) { return image; }


    public Timestamp getCreatedAt() { return createdAt; }
    public Timestamp setCreatedAt(Timestamp createdAt) { return createdAt; }

    public int getLikes() { return likes; }
    public int setLikes(int likes) { return likes; }

    public int getComments() { return comments; }
    public int setComments(int comments) { return comments; }

}
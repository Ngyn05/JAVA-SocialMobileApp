package vn.edu.ueh.socialapplication.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Post implements Serializable {
    private String postId;
    private String userId;
    private String userName;
    private String content;
    private String image;
    private Date createdAt;
    private List<String> likes;
    private int comments;

    public Post() { }

    public Post(String userId, String userName, String content, String image, Date createdAt) {
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.image = image;
        this.createdAt = createdAt;
        this.likes = new ArrayList<>();
        this.comments = 0;
    }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }


    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public List<String> getLikes() { return likes; }
    public void setLikes(List<String> likes) { this.likes = likes; }
    @Exclude
    public int getLikesCount() {
        return likes != null ? likes.size() : 0;
    }

    public int getComments() { return comments; }
    public void setComments(int comments) { this.comments = comments; }

}
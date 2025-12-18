package vn.edu.ueh.socialapplication.data.model;

import com.google.firebase.Timestamp;

import java.io.Serializable;

public class Comment implements Serializable {
    private String commentId;
    private String userId;
    private String userName;
    private String content;
    private Timestamp createdAt;

    public Comment() { }

    public Comment(String commentId, String userId, String userName, String content, Timestamp createdAt) {
        this.commentId = commentId;
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.createdAt = createdAt;
    }

    public String getCommentId() { return commentId; }
    public String setCommentId(String commentId) { return commentId; }

    public String getUserId() { return userId; }
    public String setUserId(String userId) { return userId; }

    public String getUserName() { return userName; }
    public String setUserName(String userName) { return userName; }

    public String getContent() { return content; }
    public String setContent(String content) { return content; }

    public Timestamp getCreatedAt() { return createdAt; }

    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
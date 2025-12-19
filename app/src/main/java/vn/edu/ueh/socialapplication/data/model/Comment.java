package vn.edu.ueh.socialapplication.data.model;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.Date;

public class Comment implements Serializable {
    private String commentId;
    private String userId;
    private String userName;
    private String content;
    private Date createdAt;

    public Comment() { }

    public Comment(String userId, String userName, String content, Date createdAt) {
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.createdAt = createdAt;
    }

    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
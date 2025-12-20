package vn.edu.ueh.socialapplication.data.model;

import java.io.Serializable;
import java.util.Date;

public class Comment implements Serializable {
    private String commentId;
    private String userId;
    private String userName;
    private String userAvatar; // Added this field
    private String content;
    private Date createdAt;

    public Comment() { }

    // Updated constructor to accept userAvatar
    public Comment(String userId, String userName, String userAvatar, String content, Date createdAt) {
        this.userId = userId;
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.content = content;
        this.createdAt = createdAt;
    }

    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    // Getter and setter for userAvatar
    public String getUserAvatar() { return userAvatar; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}

package vn.edu.ueh.socialapplication.data.model;

import java.util.Date;

public class Notification {
    private String id;
    private String userId; // User who receives the notification
    private String actorId; // User who triggered the notification
    private String actorName;
    private String actorAvatar;
    private String postId;
    private String message;
    private Date timestamp;
    private boolean isRead;

    public Notification() {
    }

    public Notification(String userId, String actorId, String actorName, String actorAvatar, String postId, String message) {
        this.userId = userId;
        this.actorId = actorId;
        this.actorName = actorName;
        this.actorAvatar = actorAvatar;
        this.postId = postId;
        this.message = message;
        this.timestamp = new Date();
        this.isRead = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getActorId() {
        return actorId;
    }

    public void setActorId(String actorId) {
        this.actorId = actorId;
    }

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public String getActorAvatar() {
        return actorAvatar;
    }

    public void setActorAvatar(String actorAvatar) {
        this.actorAvatar = actorAvatar;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
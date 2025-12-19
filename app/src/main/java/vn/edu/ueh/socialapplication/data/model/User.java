package vn.edu.ueh.socialapplication.data.model;

import java.io.Serializable;

public class User implements Serializable {
    private String uid;
    private String userName; // Display Name
    private String userId;   // Unique Handle (e.g., @username)
    private String email;
    private String avatar;
    private String bio;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String uid, String userName, String userId, String email, String avatar, String bio) {
        this.uid = uid;
        this.userName = userName;
        this.userId = userId;
        this.email = email;
        this.avatar = avatar;
        this.bio = bio;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}

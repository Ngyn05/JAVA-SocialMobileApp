package vn.edu.ueh.socialapplication;

public class User {
    private String userId;
    private String userName;
    private String email;
    private String avatar;
    private String bio;

    // Required empty public constructor for Firestore
    public User() {}

    public User(String userId, String userName, String email, String avatar, String bio) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.avatar = avatar;
        this.bio = bio;
    }

    // Getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}

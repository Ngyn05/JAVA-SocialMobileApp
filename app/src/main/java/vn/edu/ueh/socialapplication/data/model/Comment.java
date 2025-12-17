package vn.edu.ueh.socialapplication.data.model;

import java.io.Serializable;

public class Comment implements Serializable {
    private String userName;
    private String content;
    // Sau này có thể thêm avatarUrl, thời gian...

    public Comment(String userName, String content) {
        this.userName = userName;
        this.content = content;
    }

    public String getUserName() { return userName; }
    public String getContent() { return content; }
}
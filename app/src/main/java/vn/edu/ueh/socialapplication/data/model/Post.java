package vn.edu.ueh.socialapplication.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Post implements Serializable {
    private String userName;
    private String content;
    private String imageUrl; // Link ảnh (test)
    private List<Comment> commentList;

    public Post(String userName, String content, String imageUrl) {
        this.userName = userName;
        this.content = content;
        this.imageUrl = imageUrl;

        // 2. Khởi tạo danh sách comment khi tạo bài viết
        this.commentList = new ArrayList<>();

        // (Tùy chọn) Thêm vài comment giả sẵn cho vui
        this.commentList.add(new Comment("Bot", "Bài viết này chưa có ai comment đâu."));
        this.commentList.add(new Comment("Bot", "Bài viết này chưa có ai comment đâu."));
        this.commentList.add(new Comment("Bot", "Bài viết này chưa có ai comment đâu."));
        this.commentList.add(new Comment("Bot", "Bài viết này chưa có ai comment đâu."));
        this.commentList.add(new Comment("Bot", "Bài viết này chưa có ai comment đâu."));
        this.commentList.add(new Comment("Bot", "Bài viết này chưa có ai comment đâu."));

    }

    // Getter
    public String getUserName() { return userName; }
    public String getContent() { return content; }
    public String getImageUrl() { return imageUrl; }
    public List<Comment> getCommentList() {
        return commentList;
    }
}
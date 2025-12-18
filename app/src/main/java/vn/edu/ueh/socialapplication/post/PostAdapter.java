package vn.edu.ueh.socialapplication.post;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import vn.edu.ueh.socialapplication.R;
import vn.edu.ueh.socialapplication.data.model.Post;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> postList;
    private final OnPostClickListener listener;

    // Sửa lại Constructor: Chỉ cần nhận listener. Dữ liệu sẽ được cập nhật qua một phương thức riêng.
    public PostAdapter(List<Post> postList, OnPostClickListener listener) {
        this.postList = postList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view); // Không cần truyền listener và list vào đây nữa
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        // Lấy bài đăng tại vị trí hiện tại
        Post post = postList.get(position);
        // Gọi hàm bind để gán dữ liệu
        holder.bind(post, listener);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    /**
     * Thêm phương thức này để cập nhật danh sách bài đăng từ Activity/Fragment.
     * Đây là một cách làm rất phổ biến và linh hoạt.
     */
    public void setPosts(List<Post> newPostList) {
        this.postList = newPostList;
        notifyDataSetChanged(); // Thông báo cho Adapter rằng dữ liệu đã thay đổi để vẽ lại UI
    }

    // Định nghĩa interface cho sự kiện click
    public interface OnPostClickListener {
        void onCommentClick(Post post);
    }

    /**
     * Chuyển ViewHolder thành non-static để dễ dàng truy cập.
     * Logic bắt sự kiện click được chuyển vào hàm bind().
     */
    public class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvContent;
        ImageView imgPost;
        ImageView imgBtnComment;
        ImageView imgAvatar; // Thêm ImageView cho avatar

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUsername);
            tvContent = itemView.findViewById(R.id.tvCaption);
            imgPost = itemView.findViewById(R.id.imgPost);
            imgBtnComment = itemView.findViewById(R.id.btnComment);
            imgAvatar = itemView.findViewById(R.id.imgAvatar); // Ánh xạ avatar từ layout
        }

        // Tạo hàm bind để gán dữ liệu và bắt sự kiện
        public void bind(final Post post, final OnPostClickListener listener) {
            tvName.setText(post.getUserName());
            tvContent.setText(post.getContent());

            // Load ảnh bài đăng
            if (post.getImage() != null && !post.getImage().isEmpty()) {
                imgPost.setVisibility(View.VISIBLE);
                Glide.with(itemView.getContext())
                        .load(post.getImage())
                        .into(imgPost);
            } else {
                imgPost.setVisibility(View.GONE); // Ẩn ImageView nếu không có ảnh
            }

//            // Load ảnh đại diện (avatar)
//            // Giả sử model Post của bạn có trường `authorAvatarUrl`
//            if (post.getAvatarUrl() != null && !post.getAvatarUrl().isEmpty()) {
//                Glide.with(itemView.getContext())
//                        .load(post.getAvatarUrl())
//                        .placeholder(R.drawable.ic_default_avatar) // Ảnh chờ trong lúc tải
//                        .error(R.drawable.ic_default_avatar) // Ảnh hiển thị khi lỗi
//                        .into(imgAvatar);
//            } else {
//                // Nếu không có avatar, hiển thị ảnh mặc định
//                imgAvatar.setImageResource(R.drawable.ic_default_avatar);
//            }

            // Bắt sự kiện click cho nút comment
            imgBtnComment.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCommentClick(post);
                }
            });
        }
    }
}

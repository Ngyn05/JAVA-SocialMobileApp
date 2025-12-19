package vn.edu.ueh.socialapplication.post;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import vn.edu.ueh.socialapplication.R;
import vn.edu.ueh.socialapplication.data.model.Post;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private String currentUserId = FirebaseAuth.getInstance().getUid();
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
        Post post = postList.get(position);
        holder.bind(post, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()) {
            Post post = postList.get(position);
            for (Object payload : payloads) {
                if (payload.equals("LIKE_UPDATE")) {
                    // CHỈ cập nhật UI Like
                    if (post.getLikes().contains(currentUserId)) {
                        holder.btnLike.setImageResource(R.drawable.ic_liked);
                    } else {
                        holder.btnLike.setImageResource(R.drawable.ic_unliked);
                    }
                    holder.tvLikeCount.setText(String.valueOf(post.getLikesCount()));
                }
            }
        } else {
            // Nếu không có payload, gọi lại hàm onBindViewHolder cơ bản ở trên
            super.onBindViewHolder(holder, position, payloads);
        }
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

    public void toggleLike(int position) {
        if (currentUserId == null) return;

        Post post = postList.get(position);
        String postId = post.getPostId();

        // Kiểm tra postId trước khi gọi Firestore
        if (postId == null) {
            Log.e("PostAdapter", "PostId is null at position: " + position);
            return;
        }

        List<String> likes = post.getLikes();
        if (likes == null) likes = new ArrayList<>();

        if (likes.contains(currentUserId)) {
            likes.remove(currentUserId);
        } else {
            likes.add(currentUserId);
        }
        post.setLikes(likes);
        notifyItemChanged(position, "LIKE_UPDATE");

        // Cập nhật Database
        DocumentReference postRef = FirebaseFirestore.getInstance().collection("posts").document(postId);
        if (likes.contains(currentUserId)) {
            postRef.update("likes", FieldValue.arrayUnion(currentUserId));
        } else {
            postRef.update("likes", FieldValue.arrayRemove(currentUserId));
        }
    }


    /**
     * Chuyển ViewHolder thành non-static để dễ dàng truy cập.
     * Logic bắt sự kiện click được chuyển vào hàm bind().
     */
    public class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvContent, tvLikeCount, tvCommentCount;
        ImageView imgPost;
        ImageView btnComment, btnLike;
        ImageView imgAvatar; // Thêm ImageView cho avatar

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUsername);
            tvContent = itemView.findViewById(R.id.tvCaption);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            tvCommentCount = itemView.findViewById(R.id.tvCommentCount);
            imgPost = itemView.findViewById(R.id.imgPost);
            btnComment = itemView.findViewById(R.id.btnComment);
            btnLike = itemView.findViewById(R.id.btnLike);
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

            if (currentUserId != null && post.getLikes() != null && post.getLikes().contains(currentUserId)) {
                // TRƯỜNG HỢP ĐÃ LIKE
                btnLike.setImageResource(R.drawable.ic_liked); // Dùng icon trái tim đầy
            } else {
                // TRƯỜNG HỢP CHƯA LIKE
                btnLike.setImageResource(R.drawable.ic_unliked); // Dùng icon trái tim trống
            }

            if (tvLikeCount != null) {
                tvLikeCount.setText(String.valueOf(post.getLikesCount()));
            }

            if (tvCommentCount != null) {
                tvCommentCount.setText(String.valueOf(post.getComments()));
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
            btnComment.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCommentClick(post);
                }
            });
            btnLike.setOnClickListener(v -> {
                // Truyền position vào hàm toggleLike
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    toggleLike(position);
                }
            });
        }
    }
}

package vn.edu.ueh.socialapplication.post; // Hoặc để trong package adapter

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

    public PostAdapter(List<Post> postList, OnPostClickListener listener) {
        this.postList = postList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        // Truyền listener và list vào đây
        return new PostViewHolder(view, listener, postList);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.tvName.setText(post.getUserName());
        holder.tvContent.setText(post.getContent());

        // Dùng Glide load ảnh từ link
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(post.getImageUrl())
                    .into(holder.imgPost);
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvContent;
        ImageView imgPost;
        ImageView imgBtnComment; // Thêm nút comment

        public PostViewHolder(@NonNull View itemView, OnPostClickListener listener, List<Post> postList) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUsername);
            tvContent = itemView.findViewById(R.id.tvCaption);
            imgPost = itemView.findViewById(R.id.imgPost);
            imgBtnComment = itemView.findViewById(R.id.btnComment); // Tìm view theo ID

            // Bắt sự kiện click
            imgBtnComment.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    // Gọi hàm trong interface, truyền bài post tại vị trí đó ra ngoài
                    listener.onCommentClick(postList.get(position));
                }
            });
        }
    }

    public interface OnPostClickListener {
        void onCommentClick(Post post);
    }

    private OnPostClickListener listener; // Biến lưu listener
}
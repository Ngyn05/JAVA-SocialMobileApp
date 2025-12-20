package vn.edu.ueh.socialapplication.post;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import vn.edu.ueh.socialapplication.R;
import vn.edu.ueh.socialapplication.data.model.Post;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> posts;
    private final OnPostClickListener onPostClickListener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());

    public interface OnPostClickListener {
        void onCommentClick(Post post);
        // We can add onLikeClick here later
    }

    public PostAdapter(List<Post> posts, OnPostClickListener onPostClickListener) {
        this.posts = posts;
        this.onPostClickListener = onPostClickListener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post, onPostClickListener);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
        notifyDataSetChanged();
    }

    class PostViewHolder extends RecyclerView.ViewHolder {

        private final CircleImageView ivUserAvatar;
        private final TextView tvUsername;
        private final TextView tvPostDate;
        private final TextView tvPostContent;
        private final ShapeableImageView ivPostImage;
        private final ImageView btnLike;
        private final ImageView btnComment;
        private final TextView tvLikeCount;
        private final TextView tvCommentCount;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.iv_user_avatar);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvPostDate = itemView.findViewById(R.id.tv_post_date);
            tvPostContent = itemView.findViewById(R.id.tv_post_content);
            ivPostImage = itemView.findViewById(R.id.iv_post_image);
            btnLike = itemView.findViewById(R.id.btn_like);
            btnComment = itemView.findViewById(R.id.btn_comment);
            tvLikeCount = itemView.findViewById(R.id.tv_like_count);
            tvCommentCount = itemView.findViewById(R.id.tv_comment_count);
        }

        public void bind(Post post, OnPostClickListener listener) {
            tvUsername.setText(post.getUserName());
            tvPostContent.setText(post.getContent());

            if (post.getCreatedAt() != null) {
                tvPostDate.setText(dateFormat.format(post.getCreatedAt()));
            }

            // For now, we use a placeholder for the user avatar
            Glide.with(itemView.getContext())
                    .load(R.drawable.ic_user_placeholder)
                    .into(ivUserAvatar);

            if (post.getImage() != null && !post.getImage().isEmpty()) {
                ivPostImage.setVisibility(View.VISIBLE);
                Glide.with(itemView.getContext())
                        .load(post.getImage())
                        .into(ivPostImage);
            } else {
                ivPostImage.setVisibility(View.GONE);
            }

            // --- FIX STARTS HERE ---

            // 1. Set correct like and comment counts
            tvLikeCount.setText(String.valueOf(post.getLikesCount()));
            tvCommentCount.setText(String.valueOf(post.getComments()));

            // 2. Set correct like button state
            String currentUserId = FirebaseAuth.getInstance().getUid();
            if (currentUserId != null && post.getLikes() != null && post.getLikes().contains(currentUserId)) {
                btnLike.setImageResource(R.drawable.ic_liked);
            } else {
                btnLike.setImageResource(R.drawable.ic_unliked);
            }

            // --- FIX ENDS HERE ---

            btnComment.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCommentClick(post);
                }
            });
            
            // TODO: Add listener for like button
        }
    }
}

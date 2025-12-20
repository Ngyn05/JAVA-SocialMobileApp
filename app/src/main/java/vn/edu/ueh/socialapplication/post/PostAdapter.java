package vn.edu.ueh.socialapplication.post;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
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

    public PostAdapter(List<Post> postList, OnPostClickListener listener) {
        this.postList = postList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
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
                    if (post.getLikes().contains(currentUserId)) {
                        holder.btnLike.setImageResource(R.drawable.ic_liked);
                    } else {
                        holder.btnLike.setImageResource(R.drawable.ic_unliked);
                    }
                    holder.tvLikeCount.setText(String.valueOf(post.getLikesCount()));
                }
            }
        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public void setPosts(List<Post> newPostList) {
        this.postList = newPostList;
        notifyDataSetChanged();
    }

    public interface OnPostClickListener {
        void onCommentClick(Post post);
    }

    public void toggleLike(int position) {
        if (currentUserId == null) return;
        Post post = postList.get(position);
        String postId = post.getPostId();
        if (postId == null) return;

        List<String> likes = post.getLikes();
        if (likes == null) likes = new ArrayList<>();

        if (likes.contains(currentUserId)) {
            likes.remove(currentUserId);
        } else {
            likes.add(currentUserId);
        }
        post.setLikes(likes);
        notifyItemChanged(position, "LIKE_UPDATE");

        DocumentReference postRef = FirebaseFirestore.getInstance().collection("posts").document(postId);
        if (likes.contains(currentUserId)) {
            postRef.update("likes", FieldValue.arrayUnion(currentUserId));
        } else {
            postRef.update("likes", FieldValue.arrayRemove(currentUserId));
        }
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvContent, tvDate, tvLikeCount, tvCommentCount;
        ImageView imgPost;
        CardView cardImage;
        ImageView btnComment, btnLike;
        ImageView imgAvatar;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUsername);
            tvContent = itemView.findViewById(R.id.tvCaption);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            tvCommentCount = itemView.findViewById(R.id.tvCommentCount);
            imgPost = itemView.findViewById(R.id.imgPost);
            cardImage = itemView.findViewById(R.id.cardImage);
            btnComment = itemView.findViewById(R.id.btnComment);
            btnLike = itemView.findViewById(R.id.btnLike);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
        }

        public void bind(final Post post, final OnPostClickListener listener) {
            tvName.setText(post.getUserName());
            tvContent.setText(post.getContent());
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
            String dateStr = sdf.format(post.getCreatedAt());
            tvDate.setText(dateStr);

            if (post.getImage() != null && !post.getImage().isEmpty()) {
                cardImage.setVisibility(View.VISIBLE);
                Glide.with(itemView.getContext())
                        .load(post.getImage())
                        .into(imgPost);
            } else {
                cardImage.setVisibility(View.GONE);
            }

            if (currentUserId != null && post.getLikes() != null && post.getLikes().contains(currentUserId)) {
                btnLike.setImageResource(R.drawable.ic_liked);
            } else {
                btnLike.setImageResource(R.drawable.ic_unliked);
            }

            tvLikeCount.setText(String.valueOf(post.getLikesCount()));
            tvCommentCount.setText(String.valueOf(post.getComments()));

            btnComment.setOnClickListener(v -> {
                if (listener != null) listener.onCommentClick(post);
            });
            btnLike.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) toggleLike(position);
            });
        }
    }
}

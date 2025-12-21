package vn.edu.ueh.socialapplication.post;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import vn.edu.ueh.socialapplication.R;
import vn.edu.ueh.socialapplication.data.model.Notification;
import vn.edu.ueh.socialapplication.data.model.Post;
import vn.edu.ueh.socialapplication.data.model.User;
import vn.edu.ueh.socialapplication.data.repository.NotificationRepository;
import vn.edu.ueh.socialapplication.data.repository.UserRepository;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private String currentUserId = FirebaseAuth.getInstance().getUid();
    private List<Post> postList;
    private final OnPostClickListener listener;
    private Context context;
    private User currentUser;

    public PostAdapter(Context context, List<Post> postList, OnPostClickListener listener) {
        this.context = context;
        this.postList = postList;
        this.listener = listener;
        loadCurrentUser();
    }

    private void loadCurrentUser() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            UserRepository userRepository = new UserRepository();
            userRepository.getUser(firebaseUser.getUid(), new UserRepository.OnUserLoadedListener() {
                @Override
                public void onUserLoaded(User user) {
                    currentUser = user;
                }

                @Override
                public void onError(Exception e) {
                    // Handle error
                }
            });
        }
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
        void onEditClick(Post post);
        void onDeleteClick(Post post);
    }

    public void toggleLike(int position) {
        if (currentUserId == null) return;
        Post post = postList.get(position);
        String postId = post.getPostId();
        if (postId == null) return;

        List<String> likes = post.getLikes();
        if (likes == null) likes = new ArrayList<>();

        boolean isLiked = likes.contains(currentUserId);

        if (isLiked) {
            likes.remove(currentUserId);
        } else {
            likes.add(currentUserId);
        }
        post.setLikes(likes);
        notifyItemChanged(position, "LIKE_UPDATE");

        DocumentReference postRef = FirebaseFirestore.getInstance().collection("posts").document(postId);
        if (!isLiked) {
            postRef.update("likes", FieldValue.arrayUnion(currentUserId));
            sendLikeNotification(post);
        } else {
            postRef.update("likes", FieldValue.arrayRemove(currentUserId));
        }
    }

    private void sendLikeNotification(Post post) {
        if (currentUser != null && !post.getUserId().equals(currentUserId)) {
            String message = currentUser.getUserName() + " liked your post.";
            Notification notification = new Notification(
                    post.getUserId(),
                    currentUserId,
                    currentUser.getUserName(),
                    currentUser.getAvatar(),
                    post.getPostId(),
                    message
            );
            NotificationRepository notificationRepository = new NotificationRepository();
            notificationRepository.sendNotification(notification);
        }
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvContent, tvDate, tvLikeCount, tvCommentCount;
        ImageView imgPost;
        CardView cardImage;
        ImageView btnComment, btnLike, btnMore;
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
            btnMore = itemView.findViewById(R.id.btnMore);
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
            
            if (currentUserId != null && currentUserId.equals(post.getUserId())) {
                btnMore.setVisibility(View.VISIBLE);
            } else {
                btnMore.setVisibility(View.GONE);
            }

            btnMore.setOnClickListener(v -> showPopupMenu(v, post, listener));
        }
        
        private void showPopupMenu(View view, Post post, OnPostClickListener listener) {
            PopupMenu popup = new PopupMenu(context, view);
            popup.getMenuInflater().inflate(R.menu.post_options_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_edit) {
                    listener.onEditClick(post);
                    return true;
                } else if (itemId == R.id.menu_delete) {
                    listener.onDeleteClick(post);
                    return true;
                }
                return false;
            });
            popup.show();
        }
    }
}
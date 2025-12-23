package vn.edu.ueh.socialapplication.post;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.edu.ueh.socialapplication.R;
import vn.edu.ueh.socialapplication.data.model.Comment;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private List<Comment> commentList = new ArrayList<>();

    public void setComments(List<Comment> comments) {
        this.commentList = comments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.tvUsername.setText(comment.getUserName());
        holder.tvComment.setText(comment.getContent());

        if (comment.getCreatedAt() != null) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                String dateStr = sdf.format(comment.getCreatedAt());
                holder.tvTimestamp.setText(dateStr);
            } catch (Exception e) {
                holder.tvTimestamp.setText("");
            }
        } else {
            holder.tvTimestamp.setText("Đang gửi...");
        }
    }

    @Override
    public int getItemCount() { return commentList.size(); }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvComment, tvTimestamp;
        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}

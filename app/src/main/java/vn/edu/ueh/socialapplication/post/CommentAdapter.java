package vn.edu.ueh.socialapplication.post;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import vn.edu.ueh.socialapplication.R;
import vn.edu.ueh.socialapplication.data.model.Comment;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> commentList;

    public CommentAdapter(List<Comment> commentList) {
        this.commentList = commentList;
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
        holder.tvUser.setText(comment.getUserName());
        holder.tvContent.setText(comment.getContent());
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvUser, tvContent;
        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUser = itemView.findViewById(R.id.tvCommentUser);
            tvContent = itemView.findViewById(R.id.tvCommentContent);
        }
    }

    // Trong CommentAdapter.java
    public void setCommentList(List<Comment> newCommentList) {
        this.commentList = newCommentList;
        notifyDataSetChanged(); // Cập nhật lại toàn bộ RecyclerView
    }

}
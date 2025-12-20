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
        holder.tvUser.setText(comment.getUserName());
        holder.tvContent.setText(comment.getContent());

        // KIỂM TRA NULL Ở ĐÂY ĐỂ TRÁNH CRASH
        if (comment.getCreatedAt() != null) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                // Nếu getCreatedAt() trả về Timestamp của Firebase:
                String dateStr = sdf.format(comment.getCreatedAt());
                holder.tvDate.setText(dateStr);
            } catch (Exception e) {
                holder.tvDate.setText(""); // Nếu lỗi định dạng thì để trống
            }
        } else {
            // Nếu ngày tháng trên server chưa có (đang đợi server xử lý FieldValue.serverTimestamp())
            holder.tvDate.setText("Đang gửi...");
        }
    }

    @Override
    public int getItemCount() { return commentList.size(); }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvUser, tvContent, tvDate;
        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUser = itemView.findViewById(R.id.tvCommentUser);
            tvContent = itemView.findViewById(R.id.tvCommentContent);
            tvDate = itemView.findViewById(R.id.tvCommentDate);
        }
    }
}

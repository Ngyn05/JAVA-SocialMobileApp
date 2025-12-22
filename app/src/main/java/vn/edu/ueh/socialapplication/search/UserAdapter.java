package vn.edu.ueh.socialapplication.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import vn.edu.ueh.socialapplication.R;
import vn.edu.ueh.socialapplication.data.model.User;
import vn.edu.ueh.socialapplication.utils.ImageUtils;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUsers;
    private OnUserClickListener onUserClickListener;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public UserAdapter(Context mContext, List<User> mUsers, OnUserClickListener onUserClickListener) {
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.onUserClickListener = onUserClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = mUsers.get(position);
        holder.userName.setText(user.getUserName());
        holder.userId.setText("@" + user.getUserId());

        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            ImageUtils.loadImage(user.getAvatar(), holder.imageProfile);
        } else {
            holder.imageProfile.setImageResource(R.drawable.ic_account_circle);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onUserClickListener != null) {
                onUserClickListener.onUserClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView imageProfile;
        public TextView userName;
        public TextView userId;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProfile = itemView.findViewById(R.id.profile_image);
            userName = itemView.findViewById(R.id.userName_text);
            userId = itemView.findViewById(R.id.userId_text);
        }
    }
}

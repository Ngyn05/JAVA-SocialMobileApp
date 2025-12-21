package vn.edu.ueh.socialapplication.chat;

import android.content.Context;
import android.content.Intent;
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

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {

    private Context context;
    private List<ChatContact> chatContacts;

    public ChatListAdapter(Context context, List<ChatContact> chatContacts) {
        this.context = context;
        this.chatContacts = chatContacts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatContact contact = chatContacts.get(position);
        User user = contact.getUser();

        holder.username.setText(user.getUserName());

        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            ImageUtils.loadImage(user.getAvatar(), holder.profileImage);
        } else {
            holder.profileImage.setImageResource(R.drawable.ic_account_circle);
        }

        if (contact.getUnreadCount() > 0) {
            holder.unreadIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.unreadIndicator.setVisibility(View.GONE);
        }

        if (contact.getLastMessage() != null && !contact.getLastMessage().isEmpty()) {
            holder.lastMessage.setText(contact.getLastMessage());
        } else {
            holder.lastMessage.setText("No message");
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("otherUserId", user.getUserId());
            intent.putExtra("otherUsername", user.getUserName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return chatContacts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView profileImage;
        public TextView username;
        public TextView lastMessage;
        public View unreadIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profile_image_chat_list);
            username = itemView.findViewById(R.id.username_chat_list);
            lastMessage = itemView.findViewById(R.id.last_message_chat_list);
            unreadIndicator = itemView.findViewById(R.id.unread_indicator);
        }
    }

    public void setChatContacts(List<ChatContact> chatContacts) {
        this.chatContacts = chatContacts;
        notifyDataSetChanged();
    }
}

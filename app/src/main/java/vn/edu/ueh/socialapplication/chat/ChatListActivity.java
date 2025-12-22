package vn.edu.ueh.socialapplication.chat;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference; // Thêm import này
import com.google.firebase.firestore.DocumentSnapshot; // Thêm import này
import com.google.firebase.firestore.FirebaseFirestore; // Thêm import này
import com.google.firebase.firestore.Query; // Thêm import này

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import vn.edu.ueh.socialapplication.R;
import vn.edu.ueh.socialapplication.data.model.Message;
import vn.edu.ueh.socialapplication.data.model.User;

public class ChatListActivity extends AppCompatActivity {

    private static final String TAG = "ChatListActivity";

    private RecyclerView recyclerView;
    private ChatListAdapter chatListAdapter;
    private List<ChatContact> chatContacts;

    private FirebaseUser firebaseUser;
    private DatabaseReference chatsRef; // Vẫn dùng Realtime DB cho Chats
    private CollectionReference usersFirestoreRef; // Thay thế bằng Firestore cho Users
    private ValueEventListener chatsListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        recyclerView = findViewById(R.id.recycler_view_chat_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            Log.e(TAG, "Current Firebase user is null. Cannot load chats.");
            finish();
            return;
        }
        Log.d(TAG, "Current Firebase User ID: " + firebaseUser.getUid());

        chatContacts = new ArrayList<>();
        chatListAdapter = new ChatListAdapter(this, chatContacts);
        recyclerView.setAdapter(chatListAdapter);

        // Khởi tạo tham chiếu Firestore cho Users
        usersFirestoreRef = FirebaseFirestore.getInstance().collection("users");

        loadChatContacts();
    }

    private void loadChatContacts() {
        chatsRef = FirebaseDatabase.getInstance("https://socialapplication-7b906-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Chats");
        chatsListener = chatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange for Chats triggered. DataSnapshot exists: " + dataSnapshot.exists() + ", children count: " + dataSnapshot.getChildrenCount());
                Map<String, String> lastMessageMap = new HashMap<>();
                Map<String, Long> unreadCountMap = new HashMap<>();
                Map<String, Long> timestampMap = new HashMap<>();
                List<String> contactIds = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message chat = snapshot.getValue(Message.class);
                    if (chat == null) {
                        Log.w(TAG, "Skipping null chat object from snapshot: " + snapshot.getKey());
                        continue;
                    }
                    if (chat.getSenderId() == null || chat.getReceiverId() == null) {
                        Log.w(TAG, "Skipping chat with null sender/receiver: " + snapshot.getKey());
                        continue;
                    }

                    String contactId = null;
                    if (chat.getSenderId().equals(firebaseUser.getUid())) {
                        contactId = chat.getReceiverId();
                    } else if (chat.getReceiverId().equals(firebaseUser.getUid())) {
                        contactId = chat.getSenderId();
                    }

                    if (contactId != null && !contactId.equals(firebaseUser.getUid())) {
                        if (!contactIds.contains(contactId)) {
                            contactIds.add(contactId);
                            Log.d(TAG, "Added new contact ID: " + contactId);
                        }
                        // Always update with the latest message and timestamp
                        if (timestampMap.getOrDefault(contactId, 0L) <= chat.getTimestamp()) {
                            lastMessageMap.put(contactId, chat.getMessage());
                            timestampMap.put(contactId, chat.getTimestamp());
                        }

                        if (chat.getReceiverId().equals(firebaseUser.getUid()) && !chat.getIsRead()) { // Sử dụng getIsRead()
                            unreadCountMap.put(contactId, unreadCountMap.getOrDefault(contactId, 0L) + 1);
                        }
                    }
                }
                Log.d(TAG, "Final contactIds collected: " + contactIds.size() + ", IDs: " + contactIds);
                fetchUsersAndDisplay(contactIds, lastMessageMap, unreadCountMap, timestampMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Firebase chats data cancelled: " + databaseError.getMessage());
            }
        });
    }

    private void fetchUsersAndDisplay(List<String> contactIds, Map<String, String> lastMessageMap, Map<String, Long> unreadCountMap, Map<String, Long> timestampMap) {
        chatContacts.clear();

        if (contactIds.isEmpty()) {
            Log.d(TAG, "No contact IDs found, setting empty chat contacts.");
            chatListAdapter.setChatContacts(chatContacts);
            return;
        }

        AtomicInteger pendingUsers = new AtomicInteger(contactIds.size());
        Log.d(TAG, "Fetching details for " + contactIds.size() + " users from Firestore.");

        for (String id : contactIds) {
            usersFirestoreRef.document(id).get().addOnSuccessListener(documentSnapshot -> {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    user.setUserId(documentSnapshot.getId()); // Firestore document ID is the userId
                    String lastMessage = lastMessageMap.getOrDefault(id, "No message");
                    long unreadCount = unreadCountMap.getOrDefault(id, 0L);
                    chatContacts.add(new ChatContact(user, lastMessage, unreadCount));
                    Log.d(TAG, "Fetched user from Firestore: " + user.getUserName() + " (ID: " + user.getUserId() + ")");
                } else {
                    Log.w(TAG, "User object is null from Firestore for ID: " + id + ". Document exists: " + documentSnapshot.exists());
                }

                if (pendingUsers.decrementAndGet() == 0) {
                    Log.d(TAG, "All user fetches complete from Firestore. Total chat contacts: " + chatContacts.size());
                    Collections.sort(chatContacts, (c1, c2) -> {
                        Long t1 = timestampMap.get(c1.getUser().getUserId());
                        Long t2 = timestampMap.get(c2.getUser().getUserId());
                        if (t1 == null) t1 = 0L;
                        if (t2 == null) t2 = 0L;
                        return t2.compareTo(t1);
                    });
                    chatListAdapter.setChatContacts(chatContacts);
                }
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Failed to fetch user from Firestore for ID " + id + ": " + e.getMessage());
                if (pendingUsers.decrementAndGet() == 0) {
                    Log.d(TAG, "All user fetches complete (with errors from Firestore). Total chat contacts: " + chatContacts.size());
                    chatListAdapter.setChatContacts(chatContacts);
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatsRef != null && chatsListener != null) {
            chatsRef.removeEventListener(chatsListener);
            Log.d(TAG, "Removed chats listener.");
        }
        // Không cần remove listener cho Firestore get() vì nó là one-time operation
    }
}

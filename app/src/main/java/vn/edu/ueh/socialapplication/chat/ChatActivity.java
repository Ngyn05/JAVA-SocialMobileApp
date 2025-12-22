package vn.edu.ueh.socialapplication.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import vn.edu.ueh.socialapplication.R;
import vn.edu.ueh.socialapplication.data.model.Message;

public class ChatActivity extends AppCompatActivity {

    private TextView usernameChat;
    private EditText textSend;
    private ImageView btnSend;
    private RecyclerView recyclerViewChat;

    private FirebaseUser firebaseUser;
    private DatabaseReference reference;

    private MessageAdapter messageAdapter;
    private List<Message> mMessages;

    private String otherUserId;
    private ValueEventListener seenListener;
    private ValueEventListener messagesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initViews();
        setupToolbar();
        setupListeners();
        setupRecyclerView();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance("https://socialapplication-7b906-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();

        Intent intent = getIntent();
        otherUserId = intent.getStringExtra("otherUserId");
        String otherUsername = intent.getStringExtra("otherUsername");

        if (otherUsername != null) {
            usernameChat.setText(otherUsername);
        }

        readMessages(firebaseUser.getUid(), otherUserId);
        seenMessage(otherUserId);
    }

    private void initViews() {
        usernameChat = findViewById(R.id.username_chat);
        textSend = findViewById(R.id.text_send);
        btnSend = findViewById(R.id.btn_send);
        recyclerViewChat = findViewById(R.id.recycler_view_chat);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupListeners() {
        btnSend.setOnClickListener(v -> {
            String msg = textSend.getText().toString();
            if (!TextUtils.isEmpty(msg)) {
                sendMessage(firebaseUser.getUid(), otherUserId, msg);
            } else {
                Toast.makeText(ChatActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
            }
            textSend.setText("");
        });
    }

    private void setupRecyclerView() {
        recyclerViewChat.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerViewChat.setLayoutManager(linearLayoutManager);
        mMessages = new ArrayList<>();
        messageAdapter = new MessageAdapter(ChatActivity.this, mMessages);
        recyclerViewChat.setAdapter(messageAdapter);
    }

    private void sendMessage(String sender, String receiver, String message) {
        DatabaseReference chatRef = reference.child("Chats");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("senderId", sender);
        hashMap.put("receiverId", receiver);
        hashMap.put("message", message);
        hashMap.put("timestamp", System.currentTimeMillis());
        hashMap.put("isRead", false);

        chatRef.push().setValue(hashMap);
    }

    private void readMessages(String myid, String userid) {
        messagesListener = reference.child("Chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mMessages.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    if (message != null &&
                        ((message.getReceiverId().equals(myid) && message.getSenderId().equals(userid)) ||
                         (message.getReceiverId().equals(userid) && message.getSenderId().equals(myid)))) {
                        mMessages.add(message);
                    }
                }
                messageAdapter.setMessages(mMessages);
                if (mMessages.size() > 0) {
                    recyclerViewChat.scrollToPosition(mMessages.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void seenMessage(final String userid) {
        seenListener = reference.child("Chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    if (message != null && message.getReceiverId().equals(firebaseUser.getUid()) && 
                        message.getSenderId().equals(userid) && !message.getIsRead()) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isRead", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (reference != null) {
            if (seenListener != null) {
                reference.child("Chats").removeEventListener(seenListener);
            }
            if (messagesListener != null) {
                reference.child("Chats").removeEventListener(messagesListener);
            }
        }
    }
}

package com.example.usman.social_medely_app.Post;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.usman.social_medely_app.Utils.Messsage.Messages;
import com.example.usman.social_medely_app.Utils.MessagesAdapter;
import com.example.usman.social_medely_app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditPost extends AppCompatActivity {

    private Toolbar ChatToolbar;
    private ImageButton SendMessageButton, SendImageButton;
    private EditText ChatInputMessage;
    private RecyclerView ChatMessagesList;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;

    private String MessageReceiverID, MessageReceiverName, MessageSenderID, saveCurrentDate, saveCurrentTime;

    private TextView ChatReceiverName, userLastSeen;
    private CircleImageView ChatReceiverProfileImage;

    private DatabaseReference RootRef, UsersRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        MessageSenderID = mAuth.getCurrentUser().getUid();

        RootRef = FirebaseDatabase.getInstance().getReference();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        MessageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        MessageReceiverName = getIntent().getExtras().get("userName").toString();

        InitializeFields();
        DisplayReceiverInfo();

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessage();
            }
        });

        FetchMessages();
    }

    private void FetchMessages() {
        RootRef.child("Messages").child(MessageSenderID).child(MessageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if (dataSnapshot.exists()) {
                            Messages messages = dataSnapshot.getValue(Messages.class);
                            messagesList.add(messages);
                            messagesAdapter.notifyDataSetChanged();
                        }

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void SendMessage() {

        updateUserStatus("online");

        String messageText = ChatInputMessage.getText().toString();

        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "Please type a message first.", Toast.LENGTH_SHORT).show();
        } else {
            String message_sender_ref = "Messages/" + MessageSenderID + "/" + MessageReceiverID;
            String message_receiver_ref = "Messages/" + MessageReceiverID + "/" + MessageSenderID;

            DatabaseReference user_message_key = RootRef.child("Messages")
                    .child(MessageSenderID)
                    .child(MessageReceiverID).push();

            String message_push_id = user_message_key.getKey();

            Calendar callForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            saveCurrentDate = currentDate.format(callForDate.getTime());

            Calendar callForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm aa");
            saveCurrentTime = currentTime.format(callForTime.getTime());

            Map messageTextBody = new HashMap();
                messageTextBody.put("message", messageText);
                messageTextBody.put("time", saveCurrentTime);
                messageTextBody.put("date", saveCurrentDate);
                messageTextBody.put("type", "text");
                messageTextBody.put("from", MessageSenderID);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(message_sender_ref + "/" + message_push_id, messageTextBody);
            messageBodyDetails.put(message_receiver_ref + "/" + message_push_id, messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(EditPost.this, "Message Sent Successfully.", Toast.LENGTH_SHORT).show();
                        ChatInputMessage.setText("");
                    } else {
                        String message = task.getException().getMessage();
                        Toast.makeText(EditPost.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        ChatInputMessage.setText("");
                    }
                }
            });
        }
    }

    public void updateUserStatus(String state) {
        String saveCurrentDate, saveCurrentTime;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        Map currentStateMap = new HashMap();
        currentStateMap.put("time", saveCurrentTime);
        currentStateMap.put("date", saveCurrentDate);
        currentStateMap.put("type", state);

        UsersRef.child(MessageSenderID).child("userState")
                .updateChildren(currentStateMap);
    }

    private void DisplayReceiverInfo() {
        ChatReceiverName.setText(MessageReceiverName);

        RootRef.child("Users").child(MessageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    if (dataSnapshot.hasChild("profileimage")) {
                        final String profileImage = dataSnapshot.child("profileimage").getValue().toString();

                        Picasso.with(EditPost.this).load(profileImage).placeholder(R.drawable.profile).into(ChatReceiverProfileImage);
                    }

                    final String type = dataSnapshot.child("userState").child("type").getValue().toString();
                    final String lastDate = dataSnapshot.child("userState").child("date").getValue().toString();
                    final String lastTime = dataSnapshot.child("userState").child("time").getValue().toString();

                    if (type.equals("online")) {
                        userLastSeen.setText("online");
                    } else {
                        userLastSeen.setText("Last Seen: " + lastTime + " " + lastDate);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void InitializeFields() {
        ChatToolbar = findViewById(R.id.chat_appbar_layout);
        setSupportActionBar(ChatToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        ChatReceiverName = findViewById(R.id.chat_custom_profile_name);
        userLastSeen = findViewById(R.id.chat_custom_last_seen);
        ChatReceiverProfileImage = findViewById(R.id.chat_custom_profile_image);

        SendMessageButton = findViewById(R.id.chat_send_message_button);
        SendImageButton = findViewById(R.id.chat_send_image_file_button);
        ChatInputMessage = findViewById(R.id.chat_input_message);

        messagesAdapter = new MessagesAdapter(messagesList);
        ChatMessagesList = findViewById(R.id.chat_messages_list);
        linearLayoutManager = new LinearLayoutManager(this);

        ChatMessagesList.setHasFixedSize(true);
        ChatMessagesList.setLayoutManager(linearLayoutManager);
        ChatMessagesList.setAdapter(messagesAdapter);
    }
}

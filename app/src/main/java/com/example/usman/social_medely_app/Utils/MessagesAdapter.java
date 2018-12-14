package com.example.usman.social_medely_app.Utils;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.usman.social_medely_app.Utils.Messsage.Messages;
import com.example.usman.social_medely_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {

    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;

    private DatabaseReference usersDatabaseRef;

    public MessagesAdapter(List<Messages> userMessagesList) {
        this.userMessagesList = userMessagesList;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView ChatSenderText, ChatReceiverText;
        public CircleImageView ChatReceiverProfileImage;

        public MessageViewHolder(View itemView) {
            super(itemView);

            ChatSenderText = itemView.findViewById(R.id.chat_sender_message_text);
            ChatReceiverText = itemView.findViewById(R.id.chat_receiver_message_text);
            ChatReceiverProfileImage = itemView.findViewById(R.id.chat_message_profile_image);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_message_layout_of_users, parent, false);

        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        String ChatSenderID = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        usersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        usersDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    if (dataSnapshot.hasChild("profileimage")) {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(holder.ChatReceiverProfileImage.getContext()).load(image)
                                .placeholder(R.drawable.profile).into(holder.ChatReceiverProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (fromMessageType.equals("text")) {
            holder.ChatReceiverText.setVisibility(View.INVISIBLE);
            holder.ChatReceiverProfileImage.setVisibility(View.INVISIBLE);

            if (fromUserID.equals(ChatSenderID)) {
                holder.ChatSenderText.setBackgroundResource(R.drawable.chat_sender_message_text_bg);
                holder.ChatSenderText.setTextColor(Color.WHITE);
                holder.ChatSenderText.setGravity(Gravity.LEFT);
                holder.ChatSenderText.setText(messages.getMessage());

            } else {
                holder.ChatSenderText.setVisibility(View.INVISIBLE);
                holder.ChatReceiverText.setVisibility(View.VISIBLE);
                holder.ChatReceiverProfileImage.setVisibility(View.VISIBLE);

                holder.ChatReceiverText.setBackgroundResource(R.drawable.chat_receiver_message_text_bg);
                holder.ChatReceiverText.setTextColor(Color.WHITE);
                holder.ChatReceiverText.setGravity(Gravity.LEFT);
                holder.ChatReceiverText.setText(messages.getMessage());

            }
        }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }
}

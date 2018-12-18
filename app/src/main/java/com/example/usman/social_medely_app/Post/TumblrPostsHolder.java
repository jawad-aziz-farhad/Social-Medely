package com.example.usman.social_medely_app.Post;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.usman.social_medely_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Farhad on 18/12/2018.
 */

public class TumblrPostsHolder extends RecyclerView.ViewHolder {

    View mView;

    ImageButton LikePostButton, CommentPostButton;
    TextView DisplayNoOfLikes;
    int countLikes;
    String currentUserId;
    DatabaseReference LikesRef;

    public TumblrPostsHolder(View itemView) {
        super(itemView);
        mView = itemView;

        LikePostButton = mView.findViewById(R.id.like_button);
        CommentPostButton = mView.findViewById(R.id.comment_button);
        DisplayNoOfLikes = mView.findViewById(R.id.display_no_of_likes);

        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    }

    public void setLikeButtonStatus(final String PostKey) {
        LikesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(PostKey).hasChild(currentUserId)) {
                    countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                    LikePostButton.setImageResource(R.drawable.like);
                    DisplayNoOfLikes.setText((Integer.toString(countLikes) + (" Likes")));
                } else {
                    countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                    LikePostButton.setImageResource(R.drawable.dislike);
                    DisplayNoOfLikes.setText((Integer.toString(countLikes) + (" Likes")));

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void setFullname(String fullname) {
        TextView username = (TextView) mView.findViewById(R.id.post_user_name);
        username.setText(fullname);
    }

    public void setProfileimage(Context ctx, String profileimage) {
        CircleImageView image = (CircleImageView) mView.findViewById(R.id.post_profile_image);
        Picasso.with(ctx).load(profileimage).into(image);
    }

    public void setTime(String time) {
        TextView PostTime = (TextView) mView.findViewById(R.id.post_time);
        PostTime.setText(" - " + time);
    }

    public void setDate(String date) {
        TextView PostDate = (TextView) mView.findViewById(R.id.post_date);
        PostDate.setText(date);
    }

    public void setDescription(String description) {
        TextView PostDescription = (TextView) mView.findViewById(R.id.post_description);
        PostDescription.setText(description);
    }

    public void setPostimage(Context ctx1, String postimage) {
        ImageView PostImage = (ImageView) mView.findViewById(R.id.post_image);
        Picasso.with(ctx1).load(postimage).into(PostImage);
    }
}
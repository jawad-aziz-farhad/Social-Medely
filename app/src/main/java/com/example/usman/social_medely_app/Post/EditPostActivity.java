package com.example.usman.social_medely_app.Post;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.usman.social_medely_app.Main.MainActivity;
import com.example.usman.social_medely_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class EditPostActivity extends AppCompatActivity {

    private ImageView PostImage;
    private TextView PostDescription;
    private Button PostDeleteButton, PostEditButton;

    private String PostKey, currentUserID, databaseUserID, description, postimage;

    private FirebaseAuth mAuth;
    private DatabaseReference EditPostRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        PostKey = getIntent().getExtras().get("POST_KEY").toString();
        EditPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);

        PostImage = findViewById(R.id.edit_post_image);
        PostDescription = findViewById(R.id.edit_post_description);
        PostDeleteButton = findViewById(R.id.post_delete_button);
        PostEditButton = findViewById(R.id.post_edit_button);

        PostEditButton.setVisibility(View.INVISIBLE);
        PostDeleteButton.setVisibility(View.INVISIBLE);

        EditPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    description = dataSnapshot.child("description").getValue().toString();
                    postimage = dataSnapshot.child("postimage").getValue().toString();
                    databaseUserID = dataSnapshot.child("uid").getValue().toString();

                    PostDescription.setText(description);
                    Picasso.with(EditPostActivity.this).load(postimage).into(PostImage);

                    if (currentUserID.equals(databaseUserID)) {
                        PostEditButton.setVisibility(View.VISIBLE);
                        PostDeleteButton.setVisibility(View.VISIBLE);

                    }

                    PostEditButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            EditCurrentPost(description);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        PostDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeleteCurrentPost();
            }
        });
    }

    private void EditCurrentPost(String description) {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditPostActivity.this);
        builder.setTitle("Edit Post:");

        final EditText inputField = new EditText(EditPostActivity.this);
        inputField.setText(description);
        builder.setView(inputField);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                EditPostRef.child("description").setValue(inputField.getText().toString());
                Toast.makeText(EditPostActivity.this, "Post updated.", Toast.LENGTH_SHORT).show();

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();

            }
        });

        Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_green_dark);
    }

    private void DeleteCurrentPost() {
        EditPostRef.removeValue();
        SendUserToMainActivity();
        Toast.makeText(this, "Post has been deleted.", Toast.LENGTH_SHORT).show();

    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(EditPostActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}

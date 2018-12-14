package com.example.usman.social_medely_app.Main;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.usman.social_medely_app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText userName, fullName, countrySelector;
    private Button saveButton;
    private CircleImageView profileImage;
    private ProgressDialog loadingBar;

    private DatabaseReference UsersRef;
    private StorageReference UserProfileImageRef;

    private FirebaseAuth mAuth;
    private String currentID;

    private static int GalleryPick = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        InitFields();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetupInformation();
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);
            }
        });

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists() && dataSnapshot.hasChild("profileimage")) {
                    String image = dataSnapshot.child("profileimage").getValue().toString();
                    Picasso.with(SetupActivity.this).load(image).placeholder(R.drawable.profile).into(profileImage);
                } else {
                    Toast.makeText(SetupActivity.this, "Please select profile image first.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            loadingBar.setTitle("Profile Image");
            loadingBar.setMessage("Please wait, while we're updating your profile image.");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            Uri resultUri = result.getUri();
            StorageReference filePath = UserProfileImageRef.child(currentID + ".jpg");
            filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {

                        Intent selfIntent = new Intent (SetupActivity.this, SetupActivity.class);
                        startActivity(selfIntent);


                        Toast.makeText(SetupActivity.this, "Profile Image Uploaded", Toast.LENGTH_SHORT).show();

                        final String downloadUrl = task.getResult().getDownloadUrl().toString();
                        UsersRef.child("profileimage").setValue(downloadUrl)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(SetupActivity.this, "Image added to FB Storage", Toast.LENGTH_SHORT).show();
                                            //loadingBar.dismiss();
                                        } else {
                                            String message = task.getException().getMessage();
                                            Toast.makeText(SetupActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                            //loadingBar.dismiss();
                                        }

                                    }
                                });
                    }

                }
            });
        }
    }

    private void SetupInformation() {
        String username = userName.getText().toString();
        String full_name = fullName.getText().toString();
        String country = countrySelector.getText().toString();
        String profileimage = profileImage.toString();

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Please write your username..", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(full_name)) {
            Toast.makeText(this, "Please write your full name..", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(country)) {
            Toast.makeText(this, "Please write your country..", Toast.LENGTH_SHORT).show();
        } else {

            loadingBarSetup("Saving Information","Please wait, updating profile information");

            HashMap userMap = new HashMap();
            userMap.put("username", username);
            userMap.put("fullname", full_name);
            userMap.put("country", country);
            userMap.put("status", "hey there, i am using this App");
            userMap.put("gender", "");
            userMap.put("dob", "");
            userMap.put("relationship", "");
            UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()) {
                        SendUserToMainActivity();
                        Toast.makeText(SetupActivity.this, "Your account is created Successfully.", Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();
                    } else {
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error occurred: " + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void InitFields() {
        userName = findViewById(R.id.setup_username);
        fullName = findViewById(R.id.setup_full_name);
        countrySelector = findViewById(R.id.setup_country);
        saveButton = findViewById(R.id.setup_save_button);
        profileImage = findViewById(R.id.setup_profile_image);
        loadingBar = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        currentID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentID);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

    }

    public void loadingBarSetup(String title, String message) {
        loadingBar.setTitle(title);
        loadingBar.setMessage(message);
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(true);
    }
}

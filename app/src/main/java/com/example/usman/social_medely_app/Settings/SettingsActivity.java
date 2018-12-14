package com.example.usman.social_medely_app.Settings;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.usman.social_medely_app.Main.MainActivity;
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

public class SettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private EditText userName, userProfileName, userStatus, userCountry, userGender, userRelation, userDOB;
    private Button UpdateAccountSettingsButton;
    private CircleImageView userProfileImage;

    private DatabaseReference UsersRef;
    private StorageReference UserProfileImageRef;
    private FirebaseAuth mAuth;
    private String currentID;

    final static int GalleryPick = 1;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        InitFields();

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    if (dataSnapshot.hasChild("profileimage")) {
                        String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(SettingsActivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);
                    }

                    String myUserName = dataSnapshot.child("username").getValue().toString();
                    String myProfileName = dataSnapshot.child("fullname").getValue().toString();
                    String myProfileStatus = dataSnapshot.child("status").getValue().toString();
                    String myDOB = dataSnapshot.child("dob").getValue().toString();
                    String myCountry = dataSnapshot.child("country").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();
                    String myRelationStatus = dataSnapshot.child("relationship").getValue().toString();



                    userName.setText(myUserName);
                    userProfileName.setText(myProfileName);
                    userStatus.setText(myProfileStatus);
                    userDOB.setText(myDOB);
                    userCountry.setText(myCountry);
                    userGender.setText(myGender);
                    userRelation.setText(myRelationStatus);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        UpdateAccountSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ValidateAccountInformation();
            }
        });

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);

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

                        Intent selfIntent = new Intent (SettingsActivity.this, SettingsActivity.class);
                        startActivity(selfIntent);


                        Toast.makeText(SettingsActivity.this, "Profile Image Uploaded", Toast.LENGTH_SHORT).show();

                        final String downloadUrl = task.getResult().getDownloadUrl().toString();
                        UsersRef.child("profileimage").setValue(downloadUrl)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(SettingsActivity.this, "Image added to FB Storage", Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        } else {
                                            String message = task.getException().getMessage();
                                            Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }

                                    }
                                });
                    }

                }
            });
        }
    }

    private void InitFields() {
        mAuth = FirebaseAuth.getInstance();
        currentID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentID);

        mToolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userName = findViewById(R.id.settings_username);
        userProfileName = findViewById(R.id.settings_full_name);
        userStatus = findViewById(R.id.settings_status);
        userCountry = findViewById(R.id.settings_country);
        userGender = findViewById(R.id.settings_gender);
        userRelation = findViewById(R.id.settings_relationship_status);
        userDOB = findViewById(R.id.settings_dob);
        userProfileImage = findViewById(R.id.settings_profile_image);
        loadingBar = new ProgressDialog(this);

        UpdateAccountSettingsButton = findViewById(R.id.update_account_settings_buttons);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
    }

    private void ValidateAccountInformation() {

        String username = userName.getText().toString();
        String profilename = userProfileName.getText().toString();
        String status = userStatus.getText().toString();
        String dob = userDOB.getText().toString();
        String country = userCountry.getText().toString();
        String gender = userGender.getText().toString();
        String relation = userRelation.getText().toString();

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Please write your username", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(profilename)) {
            Toast.makeText(this, "Please write your profile name", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(status)) {
            Toast.makeText(this, "Please write your status", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(dob)) {
            Toast.makeText(this, "Please write your date of birth", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(country)) {
            Toast.makeText(this, "Please write your country", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(gender)) {
            Toast.makeText(this, "Please write your gender", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(relation)) {
            Toast.makeText(this, "Please write your relationship status", Toast.LENGTH_SHORT).show();
        } else {

            loadingBar.setTitle("Profile Image");
            loadingBar.setMessage("Please wait, while we're updating your profile image.");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            UpdateAccountInfo(username, profilename, status, dob,
                    country, gender, relation);

        }
    }

    private void UpdateAccountInfo (String username, String profilename, String status, String dob,
                                    String country, String gender, String relation){

        HashMap userMap = new HashMap();
        userMap.put("username", username);
        userMap.put("fullname", profilename);
        userMap.put("status", status);
        userMap.put("dob", dob);
        userMap.put("country", country);
        userMap.put("gender", gender);
        userMap.put("relationship", relation);

        UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    SendUserToMainActivity();
                    Toast.makeText(SettingsActivity.this, "Account Settings Updated Successfully..", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                } else {
                    String message = task.getException().getMessage();
                    Toast.makeText(SettingsActivity.this, "Error Occured, while updating information. More info: " + message, Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }
        });
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}

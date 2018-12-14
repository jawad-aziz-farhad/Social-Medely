package com.example.usman.social_medely_app.Post;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telecom.Call;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.usman.social_medely_app.Main.MainActivity;
import com.example.usman.social_medely_app.R;
import com.example.usman.social_medely_app.constants.Constants;
import com.example.usman.social_medely_app.tumblr.Tumblr;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.request.RequestBuilder;
import com.tumblr.jumblr.types.Photo;
import com.tumblr.jumblr.types.PhotoPost;
import com.tumblr.jumblr.types.Post;
import com.tumblr.jumblr.types.User;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TumblrApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class PostActivity extends AppCompatActivity {

    private static final String TAG = PostActivity.class.getSimpleName();
    private Toolbar mToolbar;

    private ImageButton SelectPostImage;
    private Button UpdatePostButton;
    private EditText PostDescription;
    private static final int GalleryPick = 1;
    private Uri ImageUri;
    private String Description;
    private String downloadUrl;

    private StorageReference PostImageRef;
    private DatabaseReference UsersRef, PostsRef;

    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;


    private String saveCurrentDate, saveCurrentTime, postRandomName, current_user_id;
    private long countPosts = 0;


    private ShareDialog shareDialog;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        PostImageRef = FirebaseStorage.getInstance().getReference();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        mToolbar = findViewById(R.id.update_post_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Post");

        SelectPostImage = findViewById(R.id.select_post_image);
        UpdatePostButton = findViewById(R.id.update_post_button);
        PostDescription = findViewById(R.id.post_description);
        loadingBar = new ProgressDialog(this);

        SelectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });
        UpdatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        UpdatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // ValidatePostInformation();

               // shareOnFacebook();

               // shareOnInstagram();

                logintoTumblr();
            }
        });
    }

    private void ValidatePostInformation() {
        Description = PostDescription.getText().toString();

        if (ImageUri == null) {
            Toast.makeText(this, "Please select an post image", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(Description)) {
            Toast.makeText(this, "Please write something about your image..", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Add New Post");
            loadingBar.setMessage("Please wait, while we adding your post.");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            SaveImageToFireBaseStorage();
        }
    }

    private void SaveImageToFireBaseStorage() {
        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(callForDate.getTime());

        Calendar callForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(callForTime.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;

        StorageReference filePath = PostImageRef.child("Post Images").child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");

        filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    downloadUrl = task.getResult().getDownloadUrl().toString();
                    Toast.makeText(PostActivity.this, "Image uploaded successfully.", Toast.LENGTH_SHORT).show();

                    SavingPostInformationToDatabase();
                } else {
                    String message = task.getException().getMessage();
                    Toast.makeText(PostActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void SavingPostInformationToDatabase() {

        PostsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    countPosts = dataSnapshot.getChildrenCount();

                } else {
                    countPosts = 0;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    String userFullName = dataSnapshot.child("fullname").getValue().toString();
                    String userProfileImage = dataSnapshot.child("profileimage").getValue().toString();

                    HashMap postMap = new HashMap();
                    postMap.put("uid", current_user_id);
                    postMap.put("date", saveCurrentDate);
                    postMap.put("time", saveCurrentTime);
                    postMap.put("description", Description);
                    postMap.put("postimage", downloadUrl);
                    postMap.put("profileimage", userProfileImage);
                    postMap.put("fullname", userFullName);
                    postMap.put("counter", countPosts);

                    PostsRef.child(current_user_id + postRandomName).updateChildren(postMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        SendUserToMainActivity();
                                        Toast.makeText(PostActivity.this, "New Post is updated successfully.", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    } else {
                                        String message = task.getException().getMessage();
                                        Toast.makeText(PostActivity.this, "Error while updating your post. More info: " + message, Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GalleryPick);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode()) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }

        else if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null) {
            ImageUri = data.getData();
            SelectPostImage.setImageURI(ImageUri);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
            SendUserToMainActivity();

        }

        return super.onOptionsItemSelected(item);
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }


    /*
    |--------------------------------------
    | Checking Facebook Status
    |--------------------------------------
    */
    private void checkFacebookStatus(){
        if(!isLoginToFacebook())
            LoginToFacebook();
        else {
            shareOnFacebook();
        }
    }

    /*
    |--------------------------------------
    | Checking Facebook Login Status
    |--------------------------------------
    */
    private boolean isLoginToFacebook(){
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null && !accessToken.isExpired();
    }

    /*
    |--------------------------------------
    | Setting Callback Manager for Facebook Login
    |--------------------------------------
    */
    private void LoginToFacebook(){
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("user_photos", "email", "public_profile", "user_posts"));
        LoginManager.getInstance().registerCallback(
                callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // Handle success
                        Log.w(TAG, "Login Result " + loginResult.toString());
                        shareOnFacebook();
                    }

                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onError(FacebookException exception) {
                    }
                }
        );
    }

    /*
    |------------------------
    | Sharing on Facebook
    |------------------------
    */
    private void shareOnFacebook(){

        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);

        Bitmap image = null;
        try {
            final InputStream imageStream = getContentResolver().openInputStream(ImageUri);
            image = BitmapFactory.decodeStream(imageStream);
        }catch (FileNotFoundException e){ e.printStackTrace(); }

        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(image)
                .setCaption(PostDescription.getText().toString())
                .build();
        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .build();

        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                Log.w(TAG, "Share Result " + result.toString());
            }
            @Override
            public void onCancel() { }

            @Override
            public void onError(FacebookException error) {

            }
        });
        shareDialog.show(content, ShareDialog.Mode.AUTOMATIC);
    }

    /*
    |------------------------
    | Sharing on Instagram
    |------------------------
    */
    private void shareOnInstagram() {
        Uri stickerAssetUri = ImageUri;
        String attributionLinkUrl = "https://www.my-aweseome-app.com/p/BhzbIOUBval/";

        // sticker asset, background colors, and attribution link
        Intent intent = new Intent("com.instagram.share.ADD_TO_STORY");
        intent.setType("image/*");
        intent.putExtra("interactive_asset_uri", stickerAssetUri);
        intent.putExtra("content_url", attributionLinkUrl);
        intent.putExtra("top_background_color",    "#a0179e");
        intent.putExtra("bottom_background_color", "#3b5999");

        // Instantiate activity and verify it will resolve implicit intent
        Activity activity = PostActivity.this;
        activity.grantUriPermission("com.instagram.android", stickerAssetUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (activity.getPackageManager().resolveActivity(intent, 0) != null) {
            activity.startActivityForResult(intent, 0);
        }
        else
            Toast.makeText(activity, "Please intall Instagram app on your device.", Toast.LENGTH_LONG).show();
    }

   /*
   |------------------------
   |  Login to Tubmblr
   |------------------------
   */
   private void logintoTumblr() {
      new TumblrPostAsyncTask().execute();

   }

   /*
   |------------------------
   | Sharing on Tumblr
   |------------------------
   */
   private void shareonTumblr() {

   }

    public class TumblrPostAsyncTask extends AsyncTask<String, String, Token> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Token doInBackground(String... params) {
            Token token = null;
            try {

                JumblrClient client = new JumblrClient("consumer_key", "consumer_secret");
                client.setToken("oauth_token", "oauth_token_secret");

                // Write the user's name
                User user = client.user();
                System.out.println(user.getName());


            } catch (Exception e) {
                Log.w(TAG, "Tumblr's login Error: " + e.getMessage());
            }

           return token;
        }

        @Override
        protected void onPostExecute(Token s) {
            super.onPostExecute(s);

            Log.w(TAG, "On Post Execute: " + s);
        }
    }

}

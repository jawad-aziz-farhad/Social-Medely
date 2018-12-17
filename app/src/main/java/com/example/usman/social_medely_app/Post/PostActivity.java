package com.example.usman.social_medely_app.Post;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
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
import com.example.usman.social_medely_app.interfaces.IResult;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.Sharer;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
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
import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.PhotoPost;
import com.tumblr.loglr.Interfaces.ExceptionHandler;
import com.tumblr.loglr.Interfaces.LoginListener;
import com.tumblr.loglr.Loglr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    private static final String TAG = PostActivity.class.getSimpleName();
    private static final int INSTAGRAM_REQUEST_CODE = 1100;
    private static final int REQUEST_STORAGE_CODE = 1000;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private ShareDialog shareDialog;
    private CallbackManager callbackManager;
    private TumblrSessionManager tumblrSessionManager;



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
               // OpenGallery();
                verifyStoragePermissions();
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
               //ValidatePostInformation();
                checkTumblrStatus();
            }
        });

        tumblrSessionManager = new TumblrSessionManager(PostActivity.this);
    }


    private void ValidatePostInformation() {
        Description = PostDescription.getText().toString();

        if (ImageUri == null) {
            Toast.makeText(this, "Please select an post image", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(Description)) {
            Toast.makeText(this, "Please write something about your image..", Toast.LENGTH_SHORT).show();
        } else {

//            loadingBar.setTitle("Add New Post");
//            loadingBar.setMessage("Please wait, while we adding your post.");
//            loadingBar.show();
//            loadingBar.setCanceledOnTouchOutside(true);

            showLoader("Add New Post","Please wait, while we adding your post.");

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

                    checkFacebookStatus();

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
                                        //SendUserToMainActivity();
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

        else if (requestCode == CallbackManagerImpl.RequestCodeOffset.Share.toRequestCode()) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }

        else if(requestCode == INSTAGRAM_REQUEST_CODE){
            Log.w(TAG, "Instagram Activity Result "+ data);
            Toast.makeText(PostActivity.this, "Successfully shared on Instagram.", Toast.LENGTH_SHORT).show();
            checkTumblrStatus();
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_STORAGE_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "Storage permission granted.");
                    OpenGallery();
                } else {
                    Toast.makeText(PostActivity.this, "User didn't allow to access Device Storage", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    /*
    |--------------------
    | Showing Loader
    |--------------------
    */
    private void showLoader(String title, String message){
        loadingBar.setTitle(title);
        loadingBar.setMessage(message);
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(true);
    }

    /*
    |---------------------------------------------
    | Checking READ WRITE PERMISSIONS FOR STORAGE
    |---------------------------------------------
    */
    public void verifyStoragePermissions() {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(PostActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    PostActivity.this,
                    PERMISSIONS_STORAGE,
                    REQUEST_STORAGE_CODE
            );
        }
        else
            OpenGallery();;
    }


    /*
    |--------------------------------------
    | Checking Facebook Status
    |--------------------------------------
    */
    private void checkFacebookStatus(){
        if(!isLoginToFacebook())
            LoginToFacebook();
        else
            shareOnFacebook();
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
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile", "user_posts"));
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
                        Toast.makeText(PostActivity.this,"Can't share the post", Toast.LENGTH_LONG).show();
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
                Toast.makeText(PostActivity.this, "Successfully shared on Facebook.", Toast.LENGTH_LONG).show();
                shareOnInstagram();
            }
            @Override
            public void onCancel() {
                Log.w(TAG, "facebook share cancelled.");
                Toast.makeText(PostActivity.this, "sorry! coudn't share on Facebook.", Toast.LENGTH_LONG).show();
                shareOnInstagram();
            }

            @Override
            public void onError(FacebookException error) {
                Log.w(TAG, "facebook share error.");
                Toast.makeText(PostActivity.this, "sorry! coudn't share on Facebook.", Toast.LENGTH_LONG).show();
                shareOnInstagram();
            }
        });
        shareDialog.show(content, ShareDialog.Mode.AUTOMATIC);
    }

    /*
    |------------------------
    | Sharing on Instagram
    |------------------------
    */
    private void shareOnInstagram(){
        Intent intent = getPackageManager().getLaunchIntentForPackage("com.instagram.android");
        if (intent != null)
        {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setPackage("com.instagram.android");
            shareIntent.putExtra(Intent.EXTRA_STREAM, ImageUri);
            shareIntent.setType("image/*");

            Activity activity = PostActivity.this;
            activity.grantUriPermission("com.instagram.android", ImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (activity.getPackageManager().resolveActivity(shareIntent, 0) != null) {
                activity.startActivityForResult(shareIntent, INSTAGRAM_REQUEST_CODE);
            }
            else
                Toast.makeText(activity, "Please intall Instagram app on your device.", Toast.LENGTH_LONG).show();
        }
        else
        {
            // bringing user to the market to download the app.
            intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("market://details?id="+"com.instagram.android"));
            startActivity(intent);
        }
    }

    /*
    |------------------------
    |  Is loggedIn to Tumblr
    |------------------------
    */
    private void checkTumblrStatus(){
        if(tumblrSessionManager.getOAuthToken() != null && tumblrSessionManager.getOauthTokenSecret() != null)
            shareOnTumblr();
        else
            loginToTumblr();
    }



  /*
  |------------------------
  |  Logging in to Tumblr
  |------------------------
  */
   private void loginToTumblr(){
       Loglr loglr = Loglr.INSTANCE;
       if(loglr != null) {
           loglr.setConsumerKey(Constants.TUBMLR_CONSUMER_KEY);
           loglr.setConsumerSecretKey(Constants.TUBMLR_CONSUMER_SECRET);
           loglr.setUrlCallBack("https://www.retime.co.uk/");
           loglr.setLoginListener(new LoginListener() {
               @Override
               public void onLoginSuccessful(com.tumblr.loglr.LoginResult loginResult) {
               String oAuthToken = loginResult.getOAuthToken();
               String oAuthTokenSecret = loginResult.getOAuthTokenSecret();
               Log.w(TAG, oAuthToken + "\n" + oAuthTokenSecret);
               tumblrSessionManager.setTumblrTokens(oAuthToken, oAuthTokenSecret);
               shareOnTumblr();

               }
           });
           loglr.setExceptionHandler(new ExceptionHandler() {
               @Override
               public void onLoginFailed(RuntimeException e) {
                   Log.e(TAG, "Loglr Exeception: " + e.getMessage());
                   Toast.makeText(PostActivity.this, "Sorry! can't login to the tumblr.", Toast.LENGTH_LONG).show();
               }
           });
           loglr.initiate(PostActivity.this);
       }
       else
           Toast.makeText(PostActivity.this, "Something went wrong while Loggin in to Tumblr.", Toast.LENGTH_LONG).show();
   }
   /*
   |------------------------
   |  Sharing On Tubmblr
   |------------------------
   */
   private void shareOnTumblr() {

      IResult iResult = new IResult() {
          @Override
          public void onSuccess(String result) {
              loadingBar.hide();
              Toast.makeText(PostActivity.this, result, Toast.LENGTH_LONG).show();
              SendUserToMainActivity();
          }

          @Override
          public void onError(String error) {
              loadingBar.hide();
              Toast.makeText(PostActivity.this, error, Toast.LENGTH_LONG).show();
          }
      };

      String caption = PostDescription.getText().toString();
      String imagePath = getPath(ImageUri);
      imagePath =  imagePath != null ? imagePath : "https://i1.fnp.com/images/pr/uae/l/i-love-you-flower-arrangement_1.jpg";

      showLoader("Tumblr","Please wait...");
      String params[] = new String[]{tumblrSessionManager.getOAuthToken(), tumblrSessionManager.getOauthTokenSecret() , caption, imagePath};
      new TumblrPostAsyncTask(iResult).execute(params);
   }

    /*
   |----------------------------------------
   | Getting String Path of Selected Image
   |----------------------------------------
   */
    private String getPath(Uri uri){
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
        if(cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;
        }
        else
            return null;
    }


   /*
   |------------------------
   | Sharing on Tumblr
   |------------------------
   */
    public static class TumblrPostAsyncTask extends AsyncTask<String, String, Boolean> {

        IResult iResult;

        TumblrPostAsyncTask(IResult iResult){
            this.iResult = iResult;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            boolean result;
            try {
                JumblrClient client = new JumblrClient(Constants.TUBMLR_CONSUMER_KEY, Constants.TUBMLR_CONSUMER_SECRET);
                client.setToken(params[0], params[1]);

                PhotoPost post = client.newPost(client.user().getBlogs().get(0).getName(),PhotoPost.class);
                Log.w(TAG, params[2] + " " + params[3]);
                post.setCaption(params[2]);
                post.setData(new File(params[3]));
                post.save();
                result = true;

            } catch (Exception e) {
                Log.w(TAG, "Tumblr's Error: " + e.getMessage());
                result = false;
            }

           return result;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            Log.w(TAG, "On Post Execute: " + success);
            if(success)
                iResult.onSuccess("Successfully Posted on Tumblr.");
            else
                iResult.onError("Sorry! couldn't Post on Tumblr.");

        }
    }

}

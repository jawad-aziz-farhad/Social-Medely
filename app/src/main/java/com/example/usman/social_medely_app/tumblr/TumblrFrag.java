package com.example.usman.social_medely_app.tumblr;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.example.usman.social_medely_app.Post.PostActivity;
import com.example.usman.social_medely_app.Post.TumblrSessionManager;
import com.example.usman.social_medely_app.R;
import com.example.usman.social_medely_app.constants.Constants;
import com.example.usman.social_medely_app.interfaces.IResult;
import com.example.usman.social_medely_app.interfaces.TumblrResult;
import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.PhotoPost;
import com.tumblr.jumblr.types.Post;
import com.tumblr.jumblr.types.User;

import java.util.List;

public class TumblrFrag extends Fragment {

    private static final String TAG = TumblrFrag.class.getSimpleName();
    private TumblrSessionManager tumblrSessionManager;
    private static String userName = null;

    public TumblrFrag() {
        // Required empty public constructor
    }
    WebView wvtumblr;
    String url="https://www.tumblr.com/login";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

//        View v=inflater.inflate(R.layout.fragment_tumblr,container,false);
//        wvtumblr=(WebView)v.findViewById(R.id.web_tumblr);
//        wvtumblr.loadUrl(url);
//        WebSettings settings=wvtumblr.getSettings();
//        settings.setJavaScriptEnabled(true);
//        wvtumblr.setWebViewClient(new WebViewClient());
//        return v;

        View v = inflater.inflate(R.layout.tumblr_posts, container,false);

//        tumblrSessionManager = new TumblrSessionManager(getActivity());
//
//        if(tumblrSessionManager.isLoggedinToTumblr())
//            getPosts();
//        else
//            loginToTumblr();
        return v;
    }

    /*
   |------------------------
   |  Getting Posts
   |------------------------
   */
    private void getPosts(){
        TumblrResult tumblrResult = new TumblrResult() {
            @Override
            public void onSuccess(List<Post> posts) {
                populatePosts(posts);
            }

            @Override
            public void onError(String error) {

            }
        };

        String params[] = new String[]{ tumblrSessionManager.getOAuthToken(), tumblrSessionManager.getOauthTokenSecret() };
        new TumblrPostsAsyncTask(tumblrResult).execute(params);
    }

    public static class TumblrPostsAsyncTask extends AsyncTask<String, String, List<Post>> {

        TumblrResult tumblrResult;

        TumblrPostsAsyncTask(TumblrResult tumblrResult){
            this.tumblrResult = tumblrResult;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Post> doInBackground(String... params) {
            JumblrClient jumblrClient = new JumblrClient(Constants.TUBMLR_CONSUMER_KEY, Constants.TUBMLR_CONSUMER_SECRET);
            jumblrClient.setToken(params[0], params[1]);
            User user = jumblrClient.user();
            userName = user.getName();
            return jumblrClient.userDashboard();
        }

        @Override
        protected void onPostExecute(List<Post> posts) {
            super.onPostExecute(posts);
            if(posts.size() > 0)
                tumblrResult.onSuccess(posts);
            else
                tumblrResult.onError("No Posts Found.");
        }
    }

    /*
    |------------------------
    | Populating Tumblr Posts
    |------------------------
    */
    private void populatePosts(List<Post> posts){
        Log.w(TAG," Total Posts: " +posts.size());
        for(Post post: posts){

            String postImage = null, description = null;

            postImage = post.getSourceUrl();
            description = post.getSourceTitle();

            Log.w(TAG, userName + " " +  postImage + " " + description);
        }
    }

    /*
    |----------------------
    | Login to Tumblr
    |-----------------------
    */
    private void loginToTumblr(){
        IResult iResult = new IResult() {
            @Override
            public void onSuccess(String result) {
                getPosts();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
            }
        };
        tumblrSessionManager.loginToTumblr(iResult);
    }


    private class MyWebViewClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            final Uri uri = Uri.parse(url);
            return true;
        }

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(request.getUrl().toString());
            return true;
        }
    }


}
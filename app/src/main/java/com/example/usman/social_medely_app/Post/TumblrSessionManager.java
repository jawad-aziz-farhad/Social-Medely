package com.example.usman.social_medely_app.Post;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.example.usman.social_medely_app.constants.Constants;
import com.example.usman.social_medely_app.interfaces.IResult;
import com.tumblr.jumblr.JumblrClient;
import com.tumblr.loglr.Interfaces.ExceptionHandler;
import com.tumblr.loglr.Interfaces.LoginListener;
import com.tumblr.loglr.Loglr;

/**
 * Created by Farhad on 17/12/2018.
 */

public class TumblrSessionManager {

    private int PRIVATE_MODE = 0;
    private static final String TAG = TumblrSessionManager.class.getSimpleName();
    private static final String PREF_NAME = "Tumblr_Tokens";
    private static final String OAUTH_TOKEN         = "oAuthToken";
    private static final String OAUTH_TOKEN_SECRET  = "oAuthTokenSecret";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;
    private IResult iResult;

    public TumblrSessionManager(Context context){
        this.context = context;
        sharedPreferences = this.context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
    }

    /*
  |------------------------
  |  Logging in to Tumblr
  |------------------------
  */
    public void loginToTumblr(final IResult iResult){
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
                    setTumblrTokens(oAuthToken, oAuthTokenSecret);
                    iResult.onSuccess("successfully logged in to tumblr.");
                }
            });
            loglr.setExceptionHandler(new ExceptionHandler() {
                @Override
                public void onLoginFailed(RuntimeException e) {
                    Log.e(TAG, "Loglr Exeception: " + e.getMessage());
                    iResult.onError(e.getMessage());
                }
            });
            loglr.initiate(context);
        }
        else
            iResult.onError("Something went wrong while Loggin in to Tumblr.");
    }

    public void setTumblrTokens(String oAuthToken, String oAuthTokenSecret){
        editor = sharedPreferences.edit();
        editor.putString(OAUTH_TOKEN, oAuthToken);
        editor.putString(OAUTH_TOKEN_SECRET, oAuthTokenSecret);
        editor.apply();
    }

    public String getOAuthToken(){ return sharedPreferences.getString(OAUTH_TOKEN, null);}
    public String getOauthTokenSecret(){ return sharedPreferences.getString(OAUTH_TOKEN_SECRET, null);}

    public boolean isLoggedinToTumblr(){
        if(getOAuthToken() != null && getOauthTokenSecret() != null)
            return true;
        else
            return false;
    }

    public void clearTumblrTokens(){
        editor = sharedPreferences.edit();
        editor.remove(OAUTH_TOKEN);
        editor.remove(OAUTH_TOKEN_SECRET);
        editor.apply();
    }


}

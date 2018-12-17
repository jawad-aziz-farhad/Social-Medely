package com.example.usman.social_medely_app.Post;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Farhad on 17/12/2018.
 */

public class TumblrSessionManager {

    private int PRIVATE_MODE = 0;
    private static final String PREF_NAME = "Tumblr_Tokens";
    private static final String OAUTH_TOKEN         = "oAuthToken";
    private static final String OAUTH_TOKEN_SECRET  = "oAuthTokenSecret";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    TumblrSessionManager(Context context){
        this.context = context;
        sharedPreferences = this.context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
    }

    public void setTumblrTokens(String oAuthToken, String oAuthTokenSecret){
        editor = sharedPreferences.edit();
        editor.putString(OAUTH_TOKEN, oAuthToken);
        editor.putString(OAUTH_TOKEN_SECRET, oAuthTokenSecret);
        editor.apply();
    }

    public String getOAuthToken(){ return sharedPreferences.getString(OAUTH_TOKEN, null);}
    public String getOauthTokenSecret(){ return sharedPreferences.getString(OAUTH_TOKEN_SECRET, null);}

    public void clearTumblrTokens(){
        editor = sharedPreferences.edit();
        editor.remove(OAUTH_TOKEN);
        editor.remove(OAUTH_TOKEN_SECRET);
        editor.apply();
    }

}

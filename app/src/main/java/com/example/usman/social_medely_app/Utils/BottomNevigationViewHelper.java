package com.example.usman.social_medely_app.Utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.MenuItem;

import com.example.usman.social_medely_app.Facebook.Facebook;
import com.example.usman.social_medely_app.Gplus.Gplus;
import com.example.usman.social_medely_app.Instagram.Instagram;
import com.example.usman.social_medely_app.R;
import com.example.usman.social_medely_app.Twitter.Twitter;
import com.example.usman.social_medely_app.tumblr.Tumblr;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class BottomNevigationViewHelper {
    private static final String TAG = "BottomNevigationViewHel";
    private Context mContext;

    public static void setupBottomNevigationView(BottomNavigationViewEx bottomNavigationViewEx){
        Log.d(TAG, "setupBottomNevigationView: Setting up BottomNevigationView");

        bottomNavigationViewEx.enableAnimation(false);
        bottomNavigationViewEx.enableItemShiftingMode(false);
        bottomNavigationViewEx.enableShiftingMode(false);
        bottomNavigationViewEx.setTextVisibility(false);

    }


    public static void enableNevigation(final Context context, BottomNavigationViewEx view) {

            view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {

                        case R.id.ic_fb:
                            Intent fb = new Intent(context, Facebook.class);
                            context.startActivity(fb);
                            break;
                        case R.id.ic_insta:
                            Intent insta = new Intent(context, Instagram.class);
                            context.startActivity(insta);
                            break;
                        case R.id.ic_twitter:
                            Intent tw = new Intent(context, Twitter.class);
                            context.startActivity(tw);
                            break;
                        case R.id.ic_tumblr:
                            Intent lnkd = new Intent(context, Tumblr.class);
                            context.startActivity(lnkd);
                            break;
                        case R.id.ic_gplus:
                            Intent gp = new Intent(context, Gplus.class);
                            context.startActivity(gp);
                            break;


                    }
                    return false;

                }

            });
        }
    }

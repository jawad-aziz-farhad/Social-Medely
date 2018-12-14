package com.example.usman.social_medely_app.Facebook;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.usman.social_medely_app.R;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

/*
import com.example.usman.socialmedley.Main.Camera;
import com.example.usman.socialmedley.Main.MainFrag;
import com.example.usman.socialmedley.Main.Message*/
//import com.example.usman.appsocial.Utils.SectionPagerAddapter;

public class Facebook extends AppCompatActivity {
    private static final String TAG = "Facebook";
    private Context mContext=Facebook.this;
    private static final int Activity_Num=0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_facebook);
        Log.d(TAG, "onCreate: started");
        setBottomNevigationView(null);
    }
    /*---------------Bottom nevigation view setup-----------*/
    private void setBottomNevigationView(BottomNavigationViewEx bottomNavigationViewEx) {
        Log.d(TAG, "setBottomNevigationView: setingupBottomNevigationView");
        bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavigationView);
        // BottomNevigationViewHelper.setupBottomNevigationView(bottomNavigationViewEx);
        //BottomNevigationViewHelper.enableNevigation(mContext, bottomNavigationViewEx );
        bottomNavigationViewEx.enableAnimation(false);
        bottomNavigationViewEx.enableItemShiftingMode(false);
        bottomNavigationViewEx.enableShiftingMode(false);
        bottomNavigationViewEx.setTextVisibility(false);
        Menu menu=bottomNavigationViewEx.getMenu();
        MenuItem menuItem=menu.getItem(Activity_Num);
        menuItem.setChecked(true);
    }


}

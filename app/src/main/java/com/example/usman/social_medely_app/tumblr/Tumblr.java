package com.example.usman.social_medely_app.tumblr;

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
import com.example.usman.socialmedlyapplication.Main.Camera;
import com.example.usman.socialmedlyapplication.Main.MainFrag;
import com.example.usman.socialmedlyapplication.Main.Message;*/
//import com.example.usman.socialmedlyapplication.Utils.SectionPagerAddapter;

public class Tumblr extends AppCompatActivity {
    private static final String TAG = "Tumblr";
    private Context mContext=Tumblr.this;
    private static final int Activity_Num=3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: started");
        setBottomNevigationView(null);

    }
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
   /* private void setupViewPager(){

        SectionPagerAddapter addapter=new SectionPagerAddapter(getSupportFragmentManager());;
        addapter.addFragment(new Camera());
        addapter.addFragment(new Message());
        addapter.addFragment(new MainFrag());
        ViewPager viewPager= (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(addapter);
        TabLayout tabLayout=(TabLayout)findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_action_camera);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_action_logo);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_action_message);

    }*/
}

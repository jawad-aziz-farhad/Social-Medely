package com.example.usman.social_medely_app.Instagram;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.usman.social_medely_app.R;

public class InstaFrag extends Fragment {
    private static final String TAG = "InstaFrag";

    public InstaFrag() {
        // Required empty public constructor
    }
    WebView wvInsta;
    String url="https://www.instagram.com/accounts/login/";


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_insta, container, false);
        wvInsta=(WebView)v.findViewById(R.id.web_insta);

        WebSettings settings=wvInsta.getSettings();
        settings.setJavaScriptEnabled(true);
        wvInsta.loadUrl(url);
        wvInsta.setWebViewClient(new WebViewClient());

        // Inflate the layout for this fragment
        return v;
    }

}

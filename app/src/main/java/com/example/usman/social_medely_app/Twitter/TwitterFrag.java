package com.example.usman.social_medely_app.Twitter;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.usman.social_medely_app.R;

public class TwitterFrag extends Fragment {
    private static final String TAG = "TwitterFrag";

    public TwitterFrag() {
        // Required empty public constructor
    }

    WebView wvTeitter;
    String url="https://mobile.twitter.com/";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_twitter,container,false);
        wvTeitter=(WebView)v.findViewById(R.id.web_tw);
        wvTeitter.loadUrl(url);
        WebSettings settings=wvTeitter.getSettings();
        settings.setJavaScriptEnabled(true);
        wvTeitter.setWebViewClient(new MyWebViewClient());

        // Inflate the layout for this fragment
        return v;
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
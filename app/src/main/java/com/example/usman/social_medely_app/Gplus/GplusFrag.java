package com.example.usman.social_medely_app.Gplus;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.usman.social_medely_app.R;

public class GplusFrag extends Fragment {
    private static final String TAG = "GplusFrag";
    WebView wvgplus;
    String url="https://plus.google.com";
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_gp,container,false);
        wvgplus=(WebView)v.findViewById(R.id.web_gp);
        wvgplus.loadUrl(url);
        WebSettings settings=wvgplus.getSettings();
        settings.setJavaScriptEnabled(true);
        wvgplus.setWebViewClient(new WebViewClient());

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

package io.syng.fragment;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;

import com.github.ksoichiro.android.observablescrollview.ObservableWebView;
import com.squareup.leakcanary.RefWatcher;

import io.syng.R;
import io.syng.app.SyngApplication;


public class WebViewFragment extends Fragment {

    private static final String HTTP_TRUSTDAVIS_METEOR_COM = "http://trustdavis.meteor.com";

    public WebViewFragment() {
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web_view, container, false);

        ObservableWebView webView = (ObservableWebView) view.findViewById(R.id.web_view);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
//
////        webView.setWebChromeClient(new WebChromeClient() {
////            public boolean onConsoleMessage(@NonNull ConsoleMessage cm) {
////
////                String output = cm.message() + " -- From line "
////                        + cm.lineNumber() + " of "
////                        + cm.sourceId();
////                MainActivity.CONSOLE_LOG += output;
////                Log.d("SyngJs", output);
////                return true;
////            }
////        });
        webView.loadUrl(HTTP_TRUSTDAVIS_METEOR_COM);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = SyngApplication.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }

}

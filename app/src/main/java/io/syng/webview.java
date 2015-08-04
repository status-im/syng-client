package io.syng;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ObservableWebView;
import com.github.ksoichiro.android.observablescrollview.ScrollState;


public class webview extends BaseActivity implements ObservableScrollViewCallbacks {

    ObservableWebView webView;
    int lastTop = 0;
    int previousToLastTop = 0;
    final int hideStep = 20;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);

        webView = (ObservableWebView) findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage cm) {

                String output = cm.message() + " -- From line "
                        + cm.lineNumber() + " of "
                        + cm.sourceId();
                MainActivity.consoleLog += output;
                Log.d("SyngJs", output);
                return true;
            }
        });
        webView.setScrollViewCallbacks(this);
//        webView.loadUrl("http://trustdavis.meteor.com");
        webView.loadUrl("file:///android_asset/boilerplate/index.html");
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {

        super.onPostCreate(savedInstanceState);
    }


    @Override
    public void onResume() {

        super.onResume();
    }

    @Override
    public void onScrollChanged(int i, boolean b, boolean b1) {

    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

        if (getSupportActionBar() == null) {
            Log.d("MyTag", "Null");

            return;
        }
        if (scrollState == ScrollState.UP) {

            if (getSupportActionBar().isShowing()) {
                Log.d("MyTag", "Hide");
                getSupportActionBar().hide();

            }
        } else if (scrollState == ScrollState.DOWN) {
            if (!getSupportActionBar().isShowing()) {
                Log.d("MyTag", "Show");
                getSupportActionBar().show();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.webview_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }
}

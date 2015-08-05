package io.syng.fragment;


import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.squareup.leakcanary.RefWatcher;

import org.apache.cordova.ConfigXmlParser;
import org.apache.cordova.CordovaInterfaceImpl;
import org.apache.cordova.CordovaPreferences;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewEngine;
import org.apache.cordova.CordovaWebViewImpl;
import org.apache.cordova.PluginEntry;
import org.apache.cordova.PluginManager;

import java.util.ArrayList;
import java.util.Locale;

import io.syng.R;
import io.syng.app.SyngApplication;


public class WebViewFragment extends Fragment {

    protected CordovaWebView webView;
    protected CordovaInterfaceImpl cordovaInterface;
    protected CordovaPreferences preferences;
    protected ArrayList<PluginEntry> pluginEntries;

    protected boolean keepRunning = true;
    protected boolean immersiveMode;

    protected View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        loadConfig();

/*
        TODO: it's can be only in activity before adding content and we may need it in Cordova Dapps
        if(!preferences.getBoolean("ShowTitle", false))
        {
            try {
                getActivity().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            } catch (Exception e) {
                System.out.print(e);
            }
        }

        if(preferences.getBoolean("SetFullscreen", false))
        {
            preferences.set("Fullscreen", true);
        }
        if(preferences.getBoolean("Fullscreen", false))
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            {
                immersiveMode = true;
            }
            else
            {
                getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        } else {
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
*/

        view = inflater.inflate(R.layout.fragment_web_view, container, false);

        cordovaInterface = makeCordovaInterface();
        if(savedInstanceState != null)
        {
            cordovaInterface.restoreInstanceState(savedInstanceState);
        }

        init();

//        loadUrl("http://trustdavis.meteor.com");
        loadUrl("file:///android_asset/boilerplate/index.html");

        return view;
    }

    public void loadUrl(String url) {
        if (webView == null) {
            init();
        }

        // If keepRunning
        this.keepRunning = preferences.getBoolean("KeepRunning", true);

        webView.loadUrlIntoView(url, true);
    }

    protected void init() {
        webView = makeWebView();
        createViews();
        if (!webView.isInitialized()) {
            webView.init(cordovaInterface, pluginEntries, preferences);
        }
        webView.getView().requestFocusFromTouch();
        cordovaInterface.onCordovaInit(webView.getPluginManager());

        // Wire the hardware volume controls to control media if desired.
        String volumePref = preferences.getString("DefaultVolumeStream", "");
        if ("media".equals(volumePref.toLowerCase(Locale.ENGLISH))) {
            getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
        }
    }

    @SuppressWarnings("deprecation")
    protected void loadConfig() {
        ConfigXmlParser parser = new ConfigXmlParser();
        parser.parse(getActivity());
        preferences = parser.getPreferences();
        preferences.setPreferencesBundle(getActivity().getIntent().getExtras());
        pluginEntries = parser.getPluginEntries();
    }

    @SuppressWarnings({"deprecation", "ResourceType"})
    protected void createViews() {
        //Why are we setting a constant as the ID? This should be investigated
        webView.getView().setId(1000);
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        p.addRule(RelativeLayout.BELOW, R.id.myToolbar);
        webView.getView().setLayoutParams(p);

        RelativeLayout rl = (RelativeLayout)view.findViewById(R.id.web_view_layout);
        rl.addView(webView.getView());

        if (preferences.contains("BackgroundColor")) {
            int backgroundColor = preferences.getInteger("BackgroundColor", Color.BLACK);
            // Background of activity:
            webView.getView().setBackgroundColor(backgroundColor);
        }

        webView.getView().requestFocusFromTouch();
    }

    protected CordovaWebView makeWebView() {
        return new CordovaWebViewImpl(makeWebViewEngine());
    }

    protected CordovaWebViewEngine makeWebViewEngine() {
        return CordovaWebViewImpl.createEngine(getActivity(), preferences);
    }

    protected CordovaInterfaceImpl makeCordovaInterface() {
        return new CordovaInterfaceImpl(getActivity()) {
            @Override
            public Object onMessage(String id, Object data) {
                return WebViewFragment.this.onMessage(id, data);
            }
        };
    }

    public Object onMessage(String id, Object data) {
        if ("onReceivedError".equals(id)) {
            //TODO: do we need handle error and show it?
/*
            JSONObject d = (JSONObject) data;
            try {
                this.onReceivedError(d.getInt("errorCode"), d.getString("description"), d.getString("url"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
*/
        } else if ("exit".equals(id)) {
            getActivity().finish();
        }
        return null;
    }

    @Override
    public void onPause() {
        super.onPause();

        if (this.webView != null) {
            boolean keepRunning = this.keepRunning;// || this.cordovaInterface.activityResultCallback != null;
            this.webView.handlePause(keepRunning);
        }
    }

/*
    TODO: it's can be only in activity and we may need it in Cordova Dapps
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //Forward to plugins
        if (this.webView != null)
            this.webView.onNewIntent(intent);
    }
*/

    @Override
    public void onResume() {
        super.onResume();

        if (this.webView == null) {
            return;
        }
        // Force window to have focus, so application always
        // receive user input. Workaround for some devices (Samsung Galaxy Note 3 at least)
        getActivity().getWindow().getDecorView().requestFocus();

        this.webView.handleResume(this.keepRunning);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (this.webView == null) {
            return;
        }
        this.webView.handleStop();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (this.webView == null) {
            return;
        }
        this.webView.handleStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (this.webView != null) {
            webView.handleDestroy();
        }
        RefWatcher refWatcher = SyngApplication.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }

/*
    TODO: it's can be only in activity and we may need it in Cordova Dapps
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && immersiveMode) {
            final int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

            getWindow().getDecorView().setSystemUiVisibility(uiOptions);
        }
    }
*/

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        cordovaInterface.setActivityResultRequestCode(requestCode);
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        cordovaInterface.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.webView == null) {
            return;
        }
        PluginManager pm = this.webView.getPluginManager();
        if (pm != null) {
            pm.onConfigurationChanged(newConfig);
        }
    }



/*
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
        Activity tmp = getActivity();
        tmp.getWindow().closeAllPanels();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = SyngApplication.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }
*/
}

package io.syng.cordova.plugin;

import org.apache.cordova.CordovaPlugin;

public class DappURL extends CordovaPlugin {

    @Override
    public boolean onOverrideUrlLoading(String url) {
        if (url.indexOf("dapp://") == 0) {
            webView.loadUrl(url.replace("dapp://", "http://"));
            return true;
        }
        return false;
    }
}
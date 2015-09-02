/*
 * Copyright (c) 2015 Jarrad Hope
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

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
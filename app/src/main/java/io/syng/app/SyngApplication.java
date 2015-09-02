/*
 * Copyright (c) 2015 Jarrad Hope
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.syng.app;

import android.content.Context;
import android.os.Message;
import android.support.multidex.MultiDexApplication;

import com.squareup.leakcanary.RefWatcher;

import org.ethereum.android.service.ConnectorHandler;
import org.ethereum.android.service.EthereumConnector;

import io.syng.service.EthereumService;
import io.syng.util.PrefsUtil;
import io.syng.util.ProfileManager;


public class SyngApplication extends MultiDexApplication implements ConnectorHandler {

    public static EthereumConnector sEthereumConnector;

    private RefWatcher refWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        PrefsUtil.initialize(this);
        ProfileManager.initialize();
//        refWatcher = LeakCanary.install(this);
        refWatcher = RefWatcher.DISABLED;

        if (sEthereumConnector == null) {
            sEthereumConnector = new EthereumConnector(this, EthereumService.class);
            sEthereumConnector.registerHandler(this);
            sEthereumConnector.bindService();
        }
        sEthereumConnector.init(ProfileManager.getCurrentProfile().getPrivateKeys());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        sEthereumConnector.removeHandler(this);
        sEthereumConnector.unbindService();
        sEthereumConnector = null;
    }


    public static RefWatcher getRefWatcher(Context context) {
        SyngApplication application = (SyngApplication) context.getApplicationContext();
        return application.refWatcher;
    }

    @Override
    public void onConnectorConnected() {
        sEthereumConnector.init(ProfileManager.getCurrentProfile().getPrivateKeys());
        sEthereumConnector.startJsonRpc();
    }

    @Override
    public void onConnectorDisconnected() {

    }

    @Override
    public String getID() {
        return "1";
    }

    @Override
    public boolean handleMessage(Message message) {
        return false;
    }
}

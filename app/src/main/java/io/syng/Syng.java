package io.syng;

import android.content.res.Configuration;

import org.ethereum.android.service.EthereumConnector;

import io.syng.entities.PreferenceManager;


public class Syng extends android.support.multidex.MultiDexApplication {

    public PreferenceManager preferenceManager;

    public static EthereumConnector ethereum = null;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
    }


    @Override public void onCreate() {

        super.onCreate();
        preferenceManager = new PreferenceManager(this);
        if (ethereum == null) {
            ethereum = new EthereumConnector(this, EthereumService.class);
            ethereum.bindService();
        }
    }

    @Override
    public void onTerminate() {

        super.onTerminate();
        preferenceManager.close();
        ethereum.unbindService();
    }
}

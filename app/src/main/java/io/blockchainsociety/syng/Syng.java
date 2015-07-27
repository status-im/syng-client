package io.blockchainsociety.syng;

import android.content.res.Configuration;

import io.blockchainsociety.syng.entities.PreferenceManager;


public class Syng extends android.support.multidex.MultiDexApplication {

    public PreferenceManager preferenceManager;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
    }


    @Override public void onCreate() {

        super.onCreate();
        preferenceManager = new PreferenceManager(this);
    }

    @Override
    public void onTerminate() {

        super.onTerminate();
        preferenceManager.close();
    }
}

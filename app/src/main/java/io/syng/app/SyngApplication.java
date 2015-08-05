package io.syng.app;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import io.syng.util.PreferenceManager;


public class SyngApplication extends MultiDexApplication {

    public PreferenceManager mPreferenceManager;

    private RefWatcher refWatcher;

    @Override public void onCreate() {
        super.onCreate();
        mPreferenceManager = new PreferenceManager(this);
        refWatcher = LeakCanary.install(this);
//        refWatcher = RefWatcher.DISABLED;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        mPreferenceManager.close();
    }


    public static RefWatcher getRefWatcher(Context context) {
        SyngApplication application = (SyngApplication) context.getApplicationContext();
        return application.refWatcher;
    }



}

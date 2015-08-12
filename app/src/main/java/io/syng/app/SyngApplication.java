package io.syng.app;

import android.content.Context;
import android.os.Message;
import android.support.multidex.MultiDexApplication;

import com.squareup.leakcanary.RefWatcher;

import org.ethereum.android.service.ConnectorHandler;
import org.ethereum.android.service.EthereumConnector;

import java.util.List;

import io.syng.entity.Dapp;
import io.syng.entity.Profile;
import io.syng.service.EthereumService;
import io.syng.util.PrefsUtil;


public class SyngApplication extends MultiDexApplication implements ConnectorHandler {

    public static EthereumConnector sEthereumConnector;

    private RefWatcher refWatcher;

    public static Profile currentProfile;

    public static void changeProfile(Profile profile) {
        List<String> privateKeys = profile.getPrivateKeys();
        sEthereumConnector.init(privateKeys);
        currentProfile = profile;
    }

    public static void addDapp(Dapp dapp) {
        currentProfile.addDapp(dapp);
        PrefsUtil.updateProfile(currentProfile);
    }

    public static void updateDapp(Dapp dapp) {
        currentProfile.updateDapp(dapp);
        PrefsUtil.updateProfile(currentProfile);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PrefsUtil.initialize(this);
//        refWatcher = LeakCanary.install(this);
        if (sEthereumConnector == null) {
            sEthereumConnector = new EthereumConnector(this, EthereumService.class);
            sEthereumConnector.registerHandler(this);
            sEthereumConnector.bindService();
        }
        refWatcher = RefWatcher.DISABLED;
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
        if (currentProfile != null) {
            sEthereumConnector.init(currentProfile.getPrivateKeys());
        }
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

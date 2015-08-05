package io.syng.service;

import android.content.Intent;

import org.ethereum.android.service.EthereumRemoteService;

public class EthereumService extends EthereumRemoteService {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

}

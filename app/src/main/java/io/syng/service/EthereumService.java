/*
 * Copyright (c) 2015 Jarrad Hope
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.syng.service;

import android.content.Intent;

import org.ethereum.android.service.EthereumRemoteService;

public class EthereumService extends EthereumRemoteService {

    public EthereumService() {
        this.ipBootstrap = "rpc0.syng.io";
        this.portBootstrap = 30303;
        this.remoteIdBootstrap = "e2f28126720452aa82f7d3083e49e6b3945502cb94d9750a15e27ee310eed6991618199f878e5fbc7dfa0e20f0af9554b41f491dc8f1dbae8f0f2d37a3a613aa";

        //this.ipBootstrap = "192.168.122.90";
        //this.portBootstrap = 30303;
        //this.remoteIdBootstrap = "aceb348f4fd7b9b5033b1703b724970d93dbc6ee8410bdc20bc0585e668d629e542cd8ec560311fc8f4a0851c914aae8945555adee73878063dfa0078cc03e07";

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

}

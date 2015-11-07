/*
 * Copyright (c) 2015 Jarrad Hope
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.syng.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDexApplication;

import com.squareup.leakcanary.RefWatcher;

import org.ethereum.android.service.ConnectorHandler;
import org.ethereum.android.service.EthereumClientMessage;
import org.ethereum.android.service.EthereumConnector;
import org.ethereum.android.service.events.BlockEventData;
import org.ethereum.android.service.events.EventData;
import org.ethereum.android.service.events.EventFlag;
import org.ethereum.android.service.events.MessageEventData;
import org.ethereum.android.service.events.PeerDisconnectEventData;
import org.ethereum.android.service.events.PendingTransactionsEventData;
import org.ethereum.android.service.events.TraceEventData;
import org.ethereum.android.service.events.VMTraceCreatedEventData;
import org.ethereum.net.p2p.HelloMessage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import io.syng.R;
import io.syng.entity.LogEntry;
import io.syng.service.EthereumService;
import io.syng.util.PrefsUtil;
import io.syng.util.ProfileManager;


public class SyngApplication extends MultiDexApplication implements ConnectorHandler {

    public static EthereumConnector sEthereumConnector;

    public static int runningMode = 2;

    private static SyngApplication instance;

    private RefWatcher refWatcher;

    @SuppressLint("SimpleDateFormat")
    private DateFormat mDateFormatter = new SimpleDateFormat("HH:mm:ss:SSS");

    public static String mConsoleLog = "";

    private boolean isRpcConnection = true;

    public boolean isEthereumConnected = false;

    public static String mHandlerIdentifier = UUID.randomUUID().toString();

    public SyngApplication() {
        instance = this;
    }

    public static Context getContext() {
        return instance;
    }

    public static void initEthereum(List<String> privateKeys) {
        SyngApplication.sEthereumConnector.init(mHandlerIdentifier, privateKeys);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PrefsUtil.initialize(this);
        ProfileManager.initialize();
//        refWatcher = LeakCanary.install(this);
        refWatcher = RefWatcher.DISABLED;

        if (SyngApplication.sEthereumConnector == null) {
            SyngApplication.sEthereumConnector = new EthereumConnector(this, EthereumService.class);
            sEthereumConnector.registerHandler(this);
            sEthereumConnector.bindService();
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        System.out.println("Terminating application");
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
        System.out.println("Connector connected");
        if (!isEthereumConnected) {
            isEthereumConnected = true;
            SyngApplication.sEthereumConnector.addListener(mHandlerIdentifier, EnumSet.allOf(EventFlag.class));
        }
        //sEthereumConnector.init(new ArrayList<String>());
    }

    @Override
    public void onConnectorDisconnected() {
        System.out.println("Connector Disconnected");
        SyngApplication.sEthereumConnector.removeListener(mHandlerIdentifier);
        isEthereumConnected = false;
    }

    @Override
    public String getID() {
        return mHandlerIdentifier;
    }

    private void addLogEntry(LogEntry logEntry) {
        Date date = new Date(logEntry.getTimeStamp());
        String message = logEntry.getMessage();
        mConsoleLog += mDateFormatter.format(date) + " -> " + (message.length() > 200 ? message.substring(0, 200) : message) + "\n";
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean handleMessage(Message message) {

        boolean isClaimed = true;
        switch (message.what) {
            case EthereumClientMessage.MSG_EVENT:
                Bundle data = message.getData();
                data.setClassLoader(EventFlag.class.getClassLoader());
                EventFlag event = (EventFlag) data.getSerializable("event");
                if (event == null)
                    return false;
                EventData eventData;
                MessageEventData messageEventData;
                switch (event) {
                    case EVENT_BLOCK:
                        BlockEventData blockEventData = data.getParcelable("data");
                        addLogEntry(new LogEntry(blockEventData.registeredTime, "Added block with " + blockEventData.receipts.size() + " transaction receipts."));
                        break;
                    case EVENT_HANDSHAKE_PEER:
                        messageEventData = data.getParcelable("data");
                        addLogEntry(new LogEntry(messageEventData.registeredTime, "Peer " + new HelloMessage(messageEventData.message).getPeerId() + " said hello"));
                        break;
                    case EVENT_NO_CONNECTIONS:
                        eventData = data.getParcelable("data");
                        //Disable no connections logging while we use rpc
                        if (!isRpcConnection) {
                            addLogEntry(new LogEntry(eventData.registeredTime, "No connections"));
                        }
                        break;
                    case EVENT_PEER_DISCONNECT:
                        PeerDisconnectEventData peerDisconnectEventData = data.getParcelable("data");
                        addLogEntry(new LogEntry(peerDisconnectEventData.registeredTime, "Peer " + peerDisconnectEventData.host + ":" + peerDisconnectEventData.port + " disconnected."));
                        break;
                    case EVENT_PENDING_TRANSACTIONS_RECEIVED:
                        PendingTransactionsEventData pendingTransactionsEventData = data.getParcelable("data");
                        addLogEntry(new LogEntry(pendingTransactionsEventData.registeredTime, "Received " + pendingTransactionsEventData.transactions.size() + " pending transactions"));
                        break;
                    case EVENT_RECEIVE_MESSAGE:
                        messageEventData = data.getParcelable("data");
                        addLogEntry(new LogEntry(messageEventData.registeredTime, "Received message: " + messageEventData.messageClass.getName()));
                        break;
                    case EVENT_SEND_MESSAGE:
                        messageEventData = data.getParcelable("data");
                        addLogEntry(new LogEntry(messageEventData.registeredTime, "Sent message: " + messageEventData.messageClass.getName()));
                        break;
                    case EVENT_SYNC_DONE:
                        eventData = data.getParcelable("data");
                        addLogEntry(new LogEntry(eventData.registeredTime, "Sync done"));
                        break;
                    case EVENT_VM_TRACE_CREATED:
                        VMTraceCreatedEventData vmTraceCreatedEventData = data.getParcelable("data");
                        addLogEntry(new LogEntry(vmTraceCreatedEventData.registeredTime,
                                "CM trace created: " + vmTraceCreatedEventData.transactionHash + " - " + vmTraceCreatedEventData.trace));
                        break;
                    case EVENT_TRACE:
                        TraceEventData traceEventData = data.getParcelable("data");
                        //System.out.println("We got a trace message: " + traceEventData.message);
                        addLogEntry(new LogEntry(traceEventData.registeredTime, traceEventData.message));
                        isClaimed = false;
                        break;
                    case EVENT_ETHEREUM_CREATED:
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                        String runningMode = sharedPref.getString(getString(R.string.pref_running_mode_key), "-1");
                        if (runningMode.equals("1")) {
                            this.runningMode = 1;
                            isRpcConnection = false;
                            sEthereumConnector.connect(null, -1, null);
                            System.out.println("connecting: " + runningMode);
                        } else {
                            this.runningMode = 2;
                            sEthereumConnector.startJsonRpc();
                            String rpcServer = sharedPref.getString(getString(R.string.pref_json_rpc_server_key), "http://rpc0.syng.io:8545/");
                            sEthereumConnector.changeJsonRpc(rpcServer);
                        }
                        System.out.println("Running mode: " + runningMode);
                        break;
                }
                break;
            default:
                isClaimed = false;
        }
        return isClaimed;
    }

    public static void setJsonRpc() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String rpcServer = sharedPref.getString(getContext().getString(R.string.pref_json_rpc_server_key), "http://rpc0.syng.io:8545/");
        sEthereumConnector.changeJsonRpc(rpcServer);
    }
}

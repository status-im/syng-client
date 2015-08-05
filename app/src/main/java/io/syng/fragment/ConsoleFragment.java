package io.syng.fragment;


import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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
import java.util.Date;
import java.util.EnumSet;
import java.util.UUID;

import io.syng.R;
import io.syng.app.SyngApplication;
import io.syng.entity.LogEntry;
import io.syng.service.EthereumService;


public class ConsoleFragment extends Fragment implements ConnectorHandler {

    private final static int CONSOLE_LENGTH = 10000;
    private final static int CONSOLE_REFRESH_MILLS = 1000 * 5; //5 sec

    private String mConsoleLog = "";

    private static EthereumConnector sEthereumConnector;

    private TextView mConsoleText;

    private Handler mHandler = new Handler();

    private String mHandlerIdentifier = UUID.randomUUID().toString();

    @SuppressLint("SimpleDateFormat")
    private DateFormat mDateFormatter = new SimpleDateFormat("HH:mm:ss:SSS");

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {

            long startTime = System.currentTimeMillis();

            int length = mConsoleLog.length();
            if (length > CONSOLE_LENGTH) {
                mConsoleLog = mConsoleLog.substring(CONSOLE_LENGTH * ((length / CONSOLE_LENGTH) - 1) + length % CONSOLE_LENGTH);
            }
            mConsoleText.setText(mConsoleLog);

            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;
            Log.d("mRunnable", Long.toString(elapsedTime));

            mHandler.postDelayed(mRunnable, CONSOLE_REFRESH_MILLS);
        }
    };

    public ConsoleFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_console, container, false);

        mConsoleText = (TextView) view.findViewById(R.id.tv_console_log);
        mConsoleText.setText(mConsoleLog);
        mConsoleText.setMovementMethod(new ScrollingMovementMethod());

        ImageView background = (ImageView) view.findViewById(R.id.iv_background);
        Glide.with(this).load(R.drawable.bg1).into(background);

        ImageView ethereumIcon = (ImageView) view.findViewById(R.id.iv_ethereum_icon);
        ImageView ethereumText = (ImageView) view.findViewById(R.id.iv_ethereum_text);
        Glide.with(this).load(R.drawable.ethereum_text).into(ethereumText);
        Glide.with(this).load(R.drawable.ethereum_icon).into(ethereumIcon);

        if (sEthereumConnector == null) {
            sEthereumConnector = new EthereumConnector(getActivity(), EthereumService.class);
        }
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacksAndMessages(null);
        sEthereumConnector.removeHandler(this);
        sEthereumConnector.removeListener(mHandlerIdentifier);
        sEthereumConnector.unbindService();
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler.post(mRunnable);
        sEthereumConnector.registerHandler(this);
        sEthereumConnector.bindService();
    }


    @Override
    public boolean handleMessage(Message message) {
        new MyAsyncTask(message).execute();
        return true;
    }

    private void addLogEntry(LogEntry logEntry) {

        long startTime = System.currentTimeMillis();

        Date date = new Date(logEntry.getTimeStamp());
        mConsoleLog += mDateFormatter.format(date) + " -> " + logEntry.getMessage() + "\n";

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        Log.d("addLogEntry", Long.toString(elapsedTime));
    }

    @Override
    public String getID() {
        return mHandlerIdentifier;
    }

    @Override
    public void onConnectorConnected() {
        sEthereumConnector.addListener(mHandlerIdentifier, EnumSet.allOf(EventFlag.class));
    }

    @Override
    public void onConnectorDisconnected() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = SyngApplication.getRefWatcher(getActivity());
        refWatcher.watch(this);
        if (getActivity().isFinishing()) {
            sEthereumConnector = null;
        }
    }

    private LogEntry myHandleMessage(Message message) {

        switch (message.what) {
            case EthereumClientMessage.MSG_EVENT:
                Bundle data = message.getData();
                data.setClassLoader(EventFlag.class.getClassLoader());
                EventFlag event = (EventFlag) data.getSerializable("event");
                EventData eventData;
                MessageEventData messageEventData;
                switch (event) {
                    case EVENT_BLOCK:
                        BlockEventData blockEventData = data.getParcelable("data");
//                        addLogEntry(blockEventData.registeredTime, "Added block with " + blockEventData.receipts.size() + " transaction receipts.");
                        return new LogEntry(blockEventData.registeredTime, "Added block with " + blockEventData.receipts.size() + " transaction receipts.");
                    case EVENT_HANDSHAKE_PEER:
                        messageEventData = data.getParcelable("data");
//                        addLogEntry(messageEventData.registeredTime, "Peer " + new HelloMessage(messageEventData.message).getPeerId() + " said hello");
                        return new LogEntry(messageEventData.registeredTime, "Peer " + new HelloMessage(messageEventData.message).getPeerId() + " said hello");
                    case EVENT_NO_CONNECTIONS:
                        eventData = data.getParcelable("data");
//                        addLogEntry(eventData.registeredTime, "No connections");
                        return new LogEntry(eventData.registeredTime, "No connections");
                    case EVENT_PEER_DISCONNECT:
                        PeerDisconnectEventData peerDisconnectEventData = data.getParcelable("data");
//                        addLogEntry(peerDisconnectEventData.registeredTime, "Peer " + peerDisconnectEventData.host + ":" + peerDisconnectEventData.port + " disconnected.");
                        return new LogEntry(peerDisconnectEventData.registeredTime, "Peer " + peerDisconnectEventData.host + ":" + peerDisconnectEventData.port + " disconnected.");
                    case EVENT_PENDING_TRANSACTIONS_RECEIVED:
                        PendingTransactionsEventData pendingTransactionsEventData = data.getParcelable("data");
//                        addLogEntry(pendingTransactionsEventData.registeredTime, "Received " + pendingTransactionsEventData.transactions.size() + " pending transactions");
                        return new LogEntry(pendingTransactionsEventData.registeredTime, "Received " + pendingTransactionsEventData.transactions.size() + " pending transactions");
                    case EVENT_RECEIVE_MESSAGE:
                        messageEventData = data.getParcelable("data");
//                        addLogEntry(messageEventData.registeredTime, "Received message: " + messageEventData.messageClass.getName());
                        return new LogEntry(messageEventData.registeredTime, "Received message: " + messageEventData.messageClass.getName());
                    case EVENT_SEND_MESSAGE:
                        messageEventData = data.getParcelable("data");
//                        addLogEntry(messageEventData.registeredTime, "Sent message: " + messageEventData.messageClass.getName());
                        return new LogEntry(messageEventData.registeredTime, "Sent message: " + messageEventData.messageClass.getName());
                    case EVENT_SYNC_DONE:
                        eventData = data.getParcelable("data");
//                        addLogEntry(eventData.registeredTime, "Sync done");
                        return new LogEntry(eventData.registeredTime, "Sync done");
                    case EVENT_VM_TRACE_CREATED:
                        VMTraceCreatedEventData vmTraceCreatedEventData = data.getParcelable("data");
//                        addLogEntry(vmTraceCreatedEventData.registeredTime, "CM trace created: " + vmTraceCreatedEventData.transactionHash + " - " + vmTraceCreatedEventData.trace);
                        return new LogEntry(vmTraceCreatedEventData.registeredTime, "CM trace created: " + vmTraceCreatedEventData.transactionHash + " - " + vmTraceCreatedEventData.trace);
                    case EVENT_TRACE:
                        TraceEventData traceEventData = data.getParcelable("data");
//                        addLogEntry(traceEventData.registeredTime, traceEventData.message);
                        return new LogEntry(traceEventData.registeredTime, traceEventData.message);
                }
                break;
        }
        return null;
    }


    private class MyAsyncTask extends AsyncTask<Void, Void, LogEntry> {

        private final Message mMessage;

        public MyAsyncTask(final Message message) {
            mMessage = Message.obtain(message);
        }

        @Override
        protected LogEntry doInBackground(Void... params) {

            long startTime = System.currentTimeMillis();

            LogEntry logEntry = myHandleMessage(mMessage);

            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;
            Log.d("doInBackground", Long.toString(elapsedTime));

            return logEntry;
        }

        @Override
        protected void onPostExecute(LogEntry logEntry) {
            super.onPostExecute(logEntry);
            if (logEntry != null) {
                addLogEntry(logEntry);
            }
        }
    }

}

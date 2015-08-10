package io.syng.fragment;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.squareup.leakcanary.RefWatcher;

import org.ethereum.android.service.ConnectorHandler;
import org.ethereum.android.service.EthereumClientMessage;
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


public class ConsoleFragment extends Fragment implements ConnectorHandler {

    private final static int CONSOLE_LENGTH = 10000;
    private final static int CONSOLE_REFRESH_MILLS = 1000 * 5; //5 sec

    private String mConsoleLog = "";

    private TextView mConsoleText;

    private Handler mHandler = new Handler();

    private String mHandlerIdentifier = UUID.randomUUID().toString();

    @SuppressLint("SimpleDateFormat")
    private DateFormat mDateFormatter = new SimpleDateFormat("HH:mm:ss:SSS");

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {

            int length = mConsoleLog.length();
            if (length > CONSOLE_LENGTH) {
                mConsoleLog = mConsoleLog.substring(CONSOLE_LENGTH * ((length / CONSOLE_LENGTH) - 1) + length % CONSOLE_LENGTH);
            }
            mConsoleText.setText(mConsoleLog);

            mHandler.postDelayed(mRunnable, CONSOLE_REFRESH_MILLS);
        }
    };


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
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacksAndMessages(null);
        SyngApplication.sEthereumConnector.removeHandler(this);
        SyngApplication.sEthereumConnector.removeListener(mHandlerIdentifier);
        SyngApplication.sEthereumConnector.unbindService();
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler.post(mRunnable);
        SyngApplication.sEthereumConnector.registerHandler(this);
        SyngApplication.sEthereumConnector.bindService();
    }


    @Override
    public boolean handleMessage(Message message) {

        boolean isClaimed = true;
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
                        addLogEntry(new LogEntry(blockEventData.registeredTime, "Added block with " + blockEventData.receipts.size() + " transaction receipts."));
                        break;
                    case EVENT_HANDSHAKE_PEER:
                        messageEventData = data.getParcelable("data");
                        addLogEntry(new LogEntry(messageEventData.registeredTime, "Peer " + new HelloMessage(messageEventData.message).getPeerId() + " said hello"));
                        break;
                    case EVENT_NO_CONNECTIONS:
                        eventData = data.getParcelable("data");
                        addLogEntry(new LogEntry(eventData.registeredTime, "No connections"));
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
                        addLogEntry(new LogEntry(traceEventData.registeredTime, traceEventData.message));
                        break;
                }
                break;
            default:
                isClaimed = false;
        }
        return isClaimed;
    }

    private void addLogEntry(LogEntry logEntry) {
        Date date = new Date(logEntry.getTimeStamp());
        mConsoleLog += mDateFormatter.format(date) + " -> " + logEntry.getMessage() + "\n";
    }

    @Override
    public String getID() {
        return mHandlerIdentifier;
    }

    @Override
    public void onConnectorConnected() {
        SyngApplication.sEthereumConnector.addListener(mHandlerIdentifier, EnumSet.allOf(EventFlag.class));
    }

    @Override
    public void onConnectorDisconnected() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = SyngApplication.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }

}

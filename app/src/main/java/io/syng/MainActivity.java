package io.syng;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
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
import org.ethereum.config.SystemProperties;
import org.ethereum.net.message.MessageFactory;
import org.ethereum.net.p2p.HelloMessage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class MainActivity extends BaseActivity implements ConnectorHandler {

    protected static String consoleLog = "";

    TextView consoleText;

    boolean isPaused = false;

    private Timer timer;
    private TimerTask timerTask;

    private static int CONSOLE_LENGTH = 10000;
    private static int CONSOLE_REFRESH = 1000;

    static EthereumConnector ethereum = null;
    protected String handlerIdentifier = UUID.randomUUID().toString();

    TextViewUpdater consoleUpdater = new TextViewUpdater();

    static DateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");

    private class TextViewUpdater implements Runnable {

        private String txt;
        @Override
        public void run() {

            consoleText.setText(txt);
        }
        public void setText(String txt) {

            this.txt = txt;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);
        }

        consoleText = (TextView) findViewById(R.id.console_log);
        consoleText.setText(MainActivity.consoleLog);
        consoleText.setMovementMethod(new ScrollingMovementMethod());

        if (ethereum == null) {
            ethereum = new EthereumConnector(this, EthereumService.class);
            ethereum.registerHandler(this);
        }
    }

    @Override
    protected void onPause() {

        super.onPause();
        isPaused = true;
        timer.cancel();
        ethereum.removeListener(handlerIdentifier);
        ethereum.unbindService();
    }

    @Override
    protected void onResume() {

        super.onResume();
        isPaused = false;
        try {
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            int length = MainActivity.consoleLog.length();
                            if (length > CONSOLE_LENGTH) {
                                MainActivity.consoleLog = MainActivity.consoleLog.substring(CONSOLE_LENGTH * ((length/CONSOLE_LENGTH)-1) + length%CONSOLE_LENGTH);
                            }
                            consoleUpdater.setText(MainActivity.consoleLog);
                            MainActivity.this.consoleText.post(consoleUpdater);
                        }
                    });
                }
            };
            timer.schedule(timerTask, 1000, CONSOLE_REFRESH);
        } catch (IllegalStateException e){
            android.util.Log.i("Damn", "resume error");
        }
        ethereum.bindService();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        ethereum.unbindService();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {

        super.onPostCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean handleMessage(Message message) {

        boolean isClaimed = true;
        switch(message.what) {
            case EthereumClientMessage.MSG_EVENT:
                Bundle data = message.getData();
                data.setClassLoader(EventFlag.class.getClassLoader());
                EventFlag event = (EventFlag)data.getSerializable("event");
                EventData eventData;
                MessageEventData messageEventData;
                switch(event) {
                    case EVENT_BLOCK:
                        BlockEventData blockEventData = data.getParcelable("data");
                        addLogEntry(blockEventData.registeredTime, "Added block with " + blockEventData.receipts.size() + " transaction receipts.");
                        break;
                    case EVENT_HANDSHAKE_PEER:
                        messageEventData = data.getParcelable("data");
                        addLogEntry(messageEventData.registeredTime, "Peer " + new HelloMessage(messageEventData.message).getPeerId() + " said hello");
                        break;
                    case EVENT_NO_CONNECTIONS:
                        eventData = data.getParcelable("data");
                        addLogEntry(eventData.registeredTime, "No connections");
                        break;
                    case EVENT_PEER_DISCONNECT:
                        PeerDisconnectEventData peerDisconnectEventData = data.getParcelable("data");
                        addLogEntry(peerDisconnectEventData.registeredTime, "Peer " + peerDisconnectEventData.host + ":" + peerDisconnectEventData.port + " disconnected.");
                        break;
                    case EVENT_PENDING_TRANSACTIONS_RECEIVED:
                        PendingTransactionsEventData pendingTransactionsEventData = data.getParcelable("data");
                        addLogEntry(pendingTransactionsEventData.registeredTime, "Received " + pendingTransactionsEventData.transactions.size() + " pending transactions");
                        break;
                    case EVENT_RECEIVE_MESSAGE:
                        messageEventData = data.getParcelable("data");
                        addLogEntry(messageEventData.registeredTime, "Received message: " + messageEventData.messageClass.getName());
                        break;
                    case EVENT_SEND_MESSAGE:
                        messageEventData = data.getParcelable("data");
                        addLogEntry(messageEventData.registeredTime, "Sent message: " + messageEventData.messageClass.getName());
                        break;
                    case EVENT_SYNC_DONE:
                        eventData = data.getParcelable("data");
                        addLogEntry(eventData.registeredTime, "Sync done");
                        break;
                    case EVENT_VM_TRACE_CREATED:
                        VMTraceCreatedEventData vmTraceCreatedEventData = data.getParcelable("data");
                        addLogEntry(vmTraceCreatedEventData.registeredTime, "CM trace created: " + vmTraceCreatedEventData.transactionHash + " - " + vmTraceCreatedEventData.trace);
                        break;
                    case EVENT_TRACE:
                        TraceEventData traceEventData = data.getParcelable("data");
                        addLogEntry(traceEventData.registeredTime, traceEventData.message);
                        break;
                }
                break;
            default:
                isClaimed = false;
        }
        return isClaimed;
    }

    protected void addLogEntry(long timestamp, String message) {

        Date date = new Date(timestamp);

        MainActivity.consoleLog += formatter.format(date) + " -> " + message + "\n";
    }

    @Override
    public String getID() {

        return handlerIdentifier;
    }

    @Override
    public void onConnectorConnected() {

        ethereum.addListener(handlerIdentifier, EnumSet.allOf(EventFlag.class));
        //ethereum.connect(SystemProperties.CONFIG.activePeerIP(), SystemProperties.CONFIG.activePeerPort(), SystemProperties.CONFIG.activePeerNodeid());
    }

    @Override
    public void onConnectorDisconnected() {

    }

}

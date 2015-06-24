package io.blockchainsociety.syng;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;

import org.ethereum.android.EthereumManager;
import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.net.p2p.HelloMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EthereumService extends Service {

    /**
     * Command to the service to register a client, receiving callbacks
     * from the service.  The Message's replyTo field must be a Messenger of
     * the client where callbacks should be sent.
     */
    static final int MSG_REGISTER_CLIENT = 1;

    /**
     * Command to the service to unregister a client, ot stop receiving callbacks
     * from the service.  The Message's replyTo field must be a Messenger of
     * the client as previously given with MSG_REGISTER_CLIENT.
     */
    static final int MSG_UNREGISTER_CLIENT = 2;


    static final int MSG_CONNECT = 10;

    static final int MSG_STOP = 11;

    static final int MSG_CONSOLE_LOG = 101;

    private static final Logger logger = LoggerFactory.getLogger("EthereumService");

    /** Keeps track of all current registered clients. */
    ArrayList<Messenger> mClients = new ArrayList<Messenger>();

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    Messenger messenger;

    static boolean isConnected = false;

    EthereumManager ethereumManager = null;

    static String consoleLog = "";

    public EthereumService() {

        ThreadGroup group = new ThreadGroup("threadGroup");
        new Thread(group, new Runnable() {
            @Override
            public void run() {
                if (!isConnected) {
                    String databaseFolder = null;
                    File extStore = Environment.getExternalStorageDirectory();
                    if (extStore.exists()) {
                        databaseFolder = extStore.getAbsolutePath();
                    } else {
                        databaseFolder = getApplicationInfo().dataDir;
                    }
                    ethereumManager = new EthereumManager(getApplicationContext(), databaseFolder);

                    ethereumManager.addListener(new MyListener());

                    try {
                        isConnected = true;
                        ethereumManager.connect(null);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }, "EthereumConnect", 32768000).start();
    }

    private void sendConsole(Messenger messenger) {

        if (consoleLog.length() > 20000) {
            consoleLog = consoleLog.substring(5000);
        }
        Message message = Message.obtain(null, MSG_CONSOLE_LOG, consoleLog);
        if (messenger == null) {
            for (int i = mClients.size() - 1; i >= 0; i--) {
                try {
                    mClients.get(i).send(message);
                } catch (RemoteException e) {
                    // The client is dead.  Remove it from the list;
                    // we are going through the list from back to front
                    // so this is safe to do inside the loop.
                    //mClients.remove(i);
                    logger.error(e.getMessage(), e);
                }
            }
        } else {
            try {
                messenger.send(message);
            } catch (RemoteException e) {
                logger.error(e.getMessage(), e);
            }
        }

    }

    private Looper serviceLooper;
    private ServiceHandler serviceHandler;

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper) {

            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case MSG_CONNECT:

                    break;
                case MSG_STOP:
                    stopSelf(msg.arg1);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private class MyListener extends EthereumListenerAdapter {


        private void addMessage(String message) {

            int messageLength = message.length();
            int logLength = consoleLog.length();
            if (logLength + messageLength > 10000) {
                consoleLog = consoleLog.substring(5000);
            }
            consoleLog += message + "\n";
        }

        @Override
        public void trace(String output) {

            addMessage(output);
        }

        @Override
        public void onBlock(Block block, List<TransactionReceipt> receipts) {

            addMessage("Added block.");
        }

        @Override
        public void onRecvMessage(org.ethereum.net.message.Message message) {

            addMessage("Received message: " + message.getCommand().name());
        }

        @Override
        public void onSendMessage(org.ethereum.net.message.Message message) {

            addMessage("Sending message: " + message.getCommand().name());
        }

        @Override
        public void onPeerDisconnect(String host, long port) {

            addMessage("Peer disconnected: " + host + ":" + port);
        }

        @Override
        public void onPendingTransactionsReceived(Set<Transaction> transactions) {

            addMessage("Pending transactions received: " + transactions.size());
        }

        @Override
        public void onSyncDone() {

            addMessage("Sync done");
        }

        @Override
        public void onNoConnections() {

            addMessage("No connections");
        }

        @Override
        public void onHandShakePeer(HelloMessage helloMessage) {

            addMessage("Peer handshaked: " + helloMessage.getCode());
        }

        @Override
        public void onVMTraceCreated(String transactionHash, String trace) {

            addMessage("Trace created: " + " - ");
        }
    }

    @Override
    public void onCreate() {

        logger.error("onCreate called");
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
        messenger = new Messenger(serviceHandler);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        //Message msg = serviceHandler.obtainMessage();
        //msg.arg1 = startId;
        //serviceHandler.sendMessage(msg);


        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {

        return messenger != null ? messenger.getBinder() : null;
    }

    @Override
    public void onDestroy() {

        if (ethereumManager != null) {
            ethereumManager.close();
            Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
        }
    }
}

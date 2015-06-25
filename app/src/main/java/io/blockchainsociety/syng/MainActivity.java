package io.blockchainsociety.syng;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.ethereum.android.interop.IAsyncCallback;
import org.ethereum.android.interop.IEthereumService;
import org.ethereum.android.interop.IListener;
import org.ethereum.config.SystemProperties;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends BaseActivity {

    protected static String consoleLog = "";

    /** Ethereum Aidl Service. */
    IEthereumService ethereumService = null;

    /** Flag indicating whether we have called bind on the service. */
    boolean isBound;

    TextView consoleText;

    boolean isPaused = false;

    private Timer timer;
    private TimerTask timerTask;

    private static int CONSOLE_LENGTH = 10000;
    private static int CONSOLE_REFRESH = 1000;

    TextViewUpdater consoleUpdater = new TextViewUpdater();

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

    IAsyncCallback.Stub getLogCallback = new IAsyncCallback.Stub() {

        public void handleResponse(String log) throws RemoteException {

            MainActivity.consoleLog = log;
            logMessage("");
        }
    };

    /**
     * Class for interacting with the main interface of the service.
     */
    protected ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {

            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            ethereumService = IEthereumService.Stub.asInterface(service);
            Toast.makeText(MainActivity.this, "service attached", Toast.LENGTH_SHORT).show();

            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                ethereumService.addListener(ethereumListener);
                ethereumService.connect(SystemProperties.CONFIG.activePeerIP(),
                        SystemProperties.CONFIG.activePeerPort(),
                        SystemProperties.CONFIG.activePeerNodeid());
                Toast.makeText(MainActivity.this, "connected to service", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                logMessage("Error adding listener: " + e.getMessage());
            }
        }

        public void onServiceDisconnected(ComponentName className) {

            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            ethereumService = null;
            Toast.makeText(MainActivity.this, "service disconnected", Toast.LENGTH_SHORT).show();
        }
    };

    IListener.Stub ethereumListener = new IListener.Stub() {

        public void trace(String message) throws RemoteException {

            MainActivity.consoleLog += message + "\n" + "\n";
        }
    };

    protected void logMessage(String message) {

        MainActivity.consoleLog += message + "\n";
        int consoleLength = MainActivity.consoleLog.length();
        if (consoleLength > 5000) {
            MainActivity.consoleLog = MainActivity.consoleLog.substring(2000);
        }

        consoleUpdater.setText(MainActivity.consoleLog);
        MainActivity.this.consoleText.post(consoleUpdater);
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
        ComponentName myService = startService(new Intent(MainActivity.this, EthereumService.class));
        doBindService();
    }

    void doBindService() {

        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        bindService(new Intent(MainActivity.this, EthereumService.class), serviceConnection, Context.BIND_AUTO_CREATE);
        isBound = true;
        Toast.makeText(MainActivity.this, "binding to service", Toast.LENGTH_SHORT).show();
    }

    void doUnbindService() {

        if (isBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            if (ethereumService != null) {
                try {
                    ethereumService.removeListener(ethereumListener);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }

            // Detach our existing connection.
            unbindService(serviceConnection);
            isBound = false;
            Toast.makeText(MainActivity.this, "unbinding from service", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {

        super.onPause();
        isPaused = true;
        timer.cancel();
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
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        doUnbindService();
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
}

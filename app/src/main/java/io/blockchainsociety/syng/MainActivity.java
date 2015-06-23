package io.blockchainsociety.syng;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends BaseActivity {

    /** Messenger for communicating with service. */
    Messenger ethereumService = null;
    /** Flag indicating whether we have called bind on the service. */
    boolean isBound;

    TextView consoleText;

    boolean isPaused = false;

    private Timer timer;
    private TimerTask timerTask;

    /**
     * Handler of incoming messages from service.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EthereumService.MSG_CONSOLE_LOG:
                    if (!isPaused) {
                        consoleText.setText((String) msg.obj);
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            ethereumService = new Messenger(service);
            Toast.makeText(MainActivity.this, "service attached", Toast.LENGTH_SHORT).show();

            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                Message msg = Message.obtain(null,
                        EthereumService.MSG_REGISTER_CLIENT);
                msg.replyTo = messenger;
                ethereumService.send(msg);
                if (!EthereumService.isConnected) {
                    msg = Message.obtain(null, EthereumService.MSG_CONNECT, this.hashCode(), 0);
                    ethereumService.send(msg);
                }
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }

            // As part of the sample, tell the user what happened.
            Toast.makeText(MainActivity.this, "connected to service", Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            ethereumService = null;
            Toast.makeText(MainActivity.this, "service disconnected", Toast.LENGTH_SHORT).show();
        }
    };


    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger messenger = new Messenger(new IncomingHandler());


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
        consoleText.setMovementMethod(new ScrollingMovementMethod());
        consoleText.setText(EthereumService.consoleLog);
        ComponentName myService = startService(new Intent(this, EthereumService.class));
        doBindService();
    }

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        bindService(new Intent(MainActivity.this,
                EthereumService.class), mConnection, Context.BIND_AUTO_CREATE);
        isBound = true;
        Toast.makeText(MainActivity.this, "binding to service", Toast.LENGTH_SHORT).show();
    }

    void doUnbindService() {
        if (isBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            if (ethereumService != null) {
                try {
                    Message msg = Message.obtain(null,
                            EthereumService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = messenger;
                    ethereumService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }

            // Detach our existing connection.
            unbindService(mConnection);
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
                            consoleText.setText(EthereumService.consoleLog);
                        }
                    });
                }
            };
            timer.schedule(timerTask, 1000, 1000);
        } catch (IllegalStateException e){
            android.util.Log.i("Damn", "resume error");
        }
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

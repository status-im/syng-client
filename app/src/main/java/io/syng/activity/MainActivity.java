package io.syng.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import io.syng.R;
import io.syng.fragment.ConsoleFragment;
import io.syng.fragment.WebViewFragment;


public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            replaceFragment(new ConsoleFragment());
        }

    }

    @Override
    protected void onDAppClick(String item) {
        switch (item) {
            case "Console":
                replaceFragment(new ConsoleFragment());
                break;
            case "DApps":
                replaceFragment(new WebViewFragment());
                break;
            case "EtherEx":
                replaceFragment(new WebViewFragment());
                break;
            case "TrustDavis":
                replaceFragment(new WebViewFragment());
                break;
            case "Augur":
                replaceFragment(new WebViewFragment());
                break;
        }
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().
                replace(R.id.container, fragment).commit();
    }

}

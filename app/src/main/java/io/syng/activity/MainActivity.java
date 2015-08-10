package io.syng.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import io.syng.R;
import io.syng.entity.Dapp;
import io.syng.fragment.ConsoleFragment;
import io.syng.fragment.WebViewFragment;


public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Intent intent = getIntent();

        if (intent.getDataString() != null && intent.getDataString().indexOf("dapp://") == 0) {
            WebViewFragment wvF = new WebViewFragment();
            Bundle args = new Bundle();
            args.putString("url", intent.getDataString());
            wvF.setArguments(args);
            replaceFragment(wvF);
        }
        else if (savedInstanceState == null) {
            replaceFragment(new ConsoleFragment());
        }

    }

    @Override
    protected void onDAppClick(Dapp dapp) {
        switch (dapp.getName()) {
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

    @SuppressWarnings("ConstantConditions")
    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().
                replace(R.id.container, fragment).commit();

        Toolbar toolbar = (Toolbar) findViewById(R.id.myToolbar);
        ImageView background = (ImageView) findViewById(R.id.iv_background);
        if (!(fragment instanceof ConsoleFragment)) {
            toolbar.setBackgroundResource(R.color.toolbar_color);
            background.setImageResource(0);
        } else {
            Glide.with(this).load(R.drawable.bg1).into(background);
            toolbar.setBackgroundResource(R.drawable.fill);
        }

    }


}

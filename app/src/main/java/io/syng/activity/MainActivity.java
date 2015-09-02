/*
 * Copyright (c) 2015 Jarrad Hope
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.syng.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import io.syng.R;
import io.syng.entity.Dapp;
import io.syng.fragment.ConsoleFragment;
import io.syng.fragment.WebViewFragment;
import io.syng.util.ProfileManager;


public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            if (!ProfileManager.getCurrentProfile().getDapps().isEmpty()) {
                Dapp dapp = ProfileManager.getCurrentProfile().getDapps().get(0);
                onDAppItemClick(dapp);
            }else{
                replaceFragment(new ConsoleFragment());
            }
        }
        processIntent(getIntent());

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
    }

    private void processIntent(Intent intent) {
        if (intent.getDataString() != null && intent.getDataString().indexOf("dapp://") == 0) {
            WebViewFragment wvF = new WebViewFragment();
            Bundle args = new Bundle();
            args.putString("url", intent.getDataString());
            wvF.setArguments(args);
            replaceFragment(wvF);
            closeDrawer();
        }
    }

    @Override
    protected void onDAppClick(Dapp dapp) {
        switch (dapp.getUrl()) {
            case "":
                replaceFragment(new ConsoleFragment());
                getSupportActionBar().setTitle(R.string.app_name);
                break;
            default:
                WebViewFragment wvF = new WebViewFragment();
                Bundle args = new Bundle();
                args.putString("url", dapp.getUrl());
                wvF.setArguments(args);
                replaceFragment(wvF);
                getSupportActionBar().setTitle(dapp.getName());
                break;
        }
    }


    @SuppressWarnings("ConstantConditions")
    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().
                replace(R.id.container, fragment).commit();
    }


}

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
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;

import org.ethereum.crypto.HashUtil;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;

import io.syng.R;
import io.syng.entity.Profile;
import io.syng.util.*;

import static org.ethereum.config.SystemProperties.CONFIG;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ImageView backgroundImageView = (ImageView) findViewById(R.id.iv_background);
        Glide.with(this).load(R.drawable.console_bg).into(backgroundImageView);

        if (PrefsUtil.isFirstLaunch()) {
            createAndSetProfile();
        } else {
            loginWallet();
        }
    }

    private void startNextActivity() {
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void createAndSetProfile() {
        GeneralUtil.showProfileCreateDialog(LoginActivity.this, false, new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                if (GeneralUtil.processCreateDialog(LoginActivity.this, dialog)) {
                    dialog.dismiss();
                    startNextActivity();
                }
            }

            @Override
            public void onNegative(MaterialDialog dialog) {
                LoginActivity.this.finish();
                dialog.dismiss();
            }
        });
    }

    private void loginWallet() {
        final Profile profile = ProfileManager.getCurrentProfile();
        GeneralUtil.showProfilePasswordRequestDialog(LoginActivity.this, profile.getName(), new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                View view = dialog.getCustomView();
                EditText passwordText = (EditText) view.findViewById(R.id.et_pass);
                String password = passwordText.getText().toString();
                if (profile.checkPassword(password)) {
                    dialog.dismiss();
                    ProfileManager.setCurrentProfile(profile, password);
                    startNextActivity();
                } else {
                    Toast.makeText(LoginActivity.this, "Password is not correct", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNegative(MaterialDialog dialog) {
                LoginActivity.this.finish();
                dialog.dismiss();
            }
        });
    }

}

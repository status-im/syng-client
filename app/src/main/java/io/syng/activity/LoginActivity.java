package io.syng.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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
            startNextActivity();
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
        MaterialDialog dialog = new MaterialDialog.Builder(LoginActivity.this)
                .title("New profile")
                .positiveText(R.string.dialog_button_create)
                .negativeText(R.string.dialog_button_cancel)
                .customView(R.layout.profile_create_dialog, true)
                .autoDismiss(false)
                .cancelable(false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        EditText name = (EditText) dialog.findViewById(R.id.et_profile_name);
                        EditText pass1 = (EditText) dialog.findViewById(R.id.et_profile_pass_1);
                        EditText pass2 = (EditText) dialog.findViewById(R.id.et_profile_pass_2);

                        String nameString = name.getText().toString();
                        String pass1String = pass1.getText().toString();
                        String pass2String = pass2.getText().toString();

                        if (TextUtils.isEmpty(nameString)) {
                            Toast.makeText(LoginActivity.this, "Profile name can't be empty", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (TextUtils.isEmpty(pass1String) || TextUtils.isEmpty(pass2String)) {
                            Toast.makeText(LoginActivity.this, "Password name can't be empty", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (!pass1.getText().toString().equals(pass2.getText().toString())) {
                            Toast.makeText(LoginActivity.this, "Passwords should be the same!", Toast.LENGTH_SHORT).show();
                        } else {
                            Profile profile = new Profile();
                            profile.setName(name.getText().toString());
                            profile.setPassword(pass1String);

                            List<String> addresses = new ArrayList<>();
                            byte[] addr = HashUtil.sha3(profile.getName().getBytes());
                            addresses.add(Hex.toHexString(addr));
                            String secret = CONFIG.coinbaseSecret();
                            byte[] cbAddr = HashUtil.sha3(secret.getBytes());
                            addresses.add(Hex.toHexString(cbAddr));
                            profile.setPrivateKeys(addresses);

                            ProfileManager.addProfile(profile);
                            ProfileManager.setCurrentProfile(profile);
                            GeneralUtil.hideKeyBoard(name, LoginActivity.this);
                            GeneralUtil.hideKeyBoard(pass1, LoginActivity.this);
                            GeneralUtil.hideKeyBoard(pass2, LoginActivity.this);
                            startNextActivity();
//                            dialog.dismiss();
                        }
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        LoginActivity.this.finish();
                        dialog.dismiss();
                    }

                }).show();
        EditText name = (EditText) dialog.findViewById(R.id.et_profile_name);
        GeneralUtil.showKeyBoard(name, LoginActivity.this);
    }

}

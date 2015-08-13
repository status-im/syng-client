package io.syng.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;

import org.ethereum.crypto.HashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import io.syng.R;
import io.syng.adapter.BackgroundArrayAdapter;
import io.syng.adapter.DAppDrawerAdapter;
import io.syng.adapter.DAppDrawerAdapter.OnDAppClickListener;
import io.syng.adapter.ProfileDrawerAdapter;
import io.syng.adapter.ProfileDrawerAdapter.OnProfileClickListener;
import io.syng.app.SyngApplication;
import io.syng.entity.Dapp;
import io.syng.entity.Profile;
import io.syng.util.GeneralUtil;
import io.syng.util.PrefsUtil;

import static android.view.View.VISIBLE;
import static org.ethereum.config.SystemProperties.CONFIG;

public abstract class BaseActivity extends AppCompatActivity implements
        OnClickListener, OnDAppClickListener, OnProfileClickListener, View.OnLongClickListener {

    private static final Logger logger = LoggerFactory.getLogger("SyngApplication");

    private static final int DRAWER_CLOSE_DELAY_SHORT = 200;
    private static final int DRAWER_CLOSE_DELAY_LONG = 400;

    private static final String CONTRIBUTE_LINK = "https://github.com/syng-io";
    private static final String CONTINUE_SEARCH_LINK = "dapp://syng.io/store?q=search%20query";

    private ActionBarDrawerToggle mDrawerToggle;

    private EditText mSearchTextView;
    private RecyclerView mDAppsRecyclerView;
    private RecyclerView mProfilesRecyclerView;
    private DrawerLayout mDrawerLayout;

    private DAppDrawerAdapter mDAppsDrawerAdapter;
    private ProfileDrawerAdapter mProfileDrawerAdapter;

    private View mFrontView;
    private View mBackView;

    private Handler mHandler = new Handler();

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    };
    private List<Profile> mProfiles;
    private List<Dapp> mDApps;
    private ImageView mHeaderImageView;

    protected abstract void onDAppClick(Dapp dapp);


    @SuppressLint("InflateParams")
    @Override
    public void setContentView(final int layoutResID) {
        LayoutInflater inflater = getLayoutInflater();
        mDrawerLayout = (DrawerLayout) inflater.inflate(R.layout.drawer, null, false);

        super.setContentView(mDrawerLayout);

        FrameLayout content = (FrameLayout) findViewById(R.id.content);

        Toolbar toolbar = (Toolbar) inflater.inflate(layoutResID, content, true).findViewById(R.id.myToolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(android.R.color.black));
        }

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar,
                R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                GeneralUtil.hideKeyBoard(mSearchTextView, BaseActivity.this);
                if (!isDrawerFrontViewActive()) {
                    flipDrawer();
                }
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mSearchTextView = (EditText) findViewById(R.id.search);
        initSearch();

        findViewById(R.id.ll_import_wallet).setOnClickListener(this);
        findViewById(R.id.ll_settings).setOnClickListener(this);
        findViewById(R.id.ll_contribute).setOnClickListener(this);
        findViewById(R.id.drawer_header_item).setOnClickListener(this);
        findViewById(R.id.drawer_header_item).setOnLongClickListener(this);
        mFrontView = findViewById(R.id.ll_front_view);
        mBackView = findViewById(R.id.ll_back_view);

        mProfilesRecyclerView = (RecyclerView) findViewById(R.id.accounts_drawer_recycler_view);
        RecyclerView.LayoutManager layoutManager2 = new LinearLayoutManager(this);
        mProfilesRecyclerView.setLayoutManager(layoutManager2);
        initProfiles();

        mDAppsRecyclerView = (RecyclerView) findViewById(R.id.dapd_drawer_recycler_view);
        mDAppsRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(this);
        mDAppsRecyclerView.setLayoutManager(layoutManager1);
        initDApps();

        mHeaderImageView = (ImageView) findViewById(R.id.iv_header);
        Glide.with(this).load(R.drawable.bg0).into(mHeaderImageView);

        GeneralUtil.showWarningDialogIfNeed(this);
    }


    private void initProfiles() {
        mProfiles = PrefsUtil.getProfiles();
        // Add default cow account if not present
        if (mProfiles.isEmpty()) {
            Profile profile = new Profile();
            profile.setName("Cow");
            // Add default cow and monkey addresses
            List<String> addresses = new ArrayList<>();
            byte[] cowAddr = HashUtil.sha3("cow".getBytes());
            addresses.add(Hex.toHexString(cowAddr));
            String secret = CONFIG.coinbaseSecret();
            byte[] cbAddr = HashUtil.sha3(secret.getBytes());
            addresses.add(Hex.toHexString(cbAddr));
            profile.setPrivateKeys(addresses);
            PrefsUtil.saveProfile(profile);
            mProfiles.add(profile);
        }
        mProfileDrawerAdapter = new ProfileDrawerAdapter(this, mProfiles, this);
        mProfilesRecyclerView.setAdapter(mProfileDrawerAdapter);
        if (SyngApplication.sCurrentProfile == null) {
            SyngApplication.changeProfile(mProfiles.get(0));
        }
        updateCurrentProfileName(SyngApplication.sCurrentProfile.getName());
    }


    private void initDApps() {
        mDApps = new ArrayList<>();
        if (SyngApplication.sCurrentProfile != null) {
            mDApps = SyngApplication.sCurrentProfile.getDapps();
        }
        updateAppList(mSearchTextView.getText().toString());
    }


    private void closeDrawer(int delayMills) {
        mHandler.postDelayed(mRunnable, delayMills);
    }

    protected void closeDrawer() {
        mHandler.postDelayed(mRunnable, DRAWER_CLOSE_DELAY_SHORT);
    }

    protected void changeProfile(Profile profile) {
        updateCurrentProfileName(profile.getName());
        SyngApplication.changeProfile(profile);
        initDApps();
    }

    protected void updateCurrentProfileName(String name) {
        TextView textView = (TextView) findViewById(R.id.tv_name);
        textView.setText(name);
    }

    protected void requestChangeProfile(final Profile profile) {

        new MaterialDialog.Builder(BaseActivity.this)
                .title(R.string.request_profile_password)
                .customView(R.layout.profile_password, true)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .contentColor(getResources().getColor(R.color.accent))
                .dividerColorRes(R.color.accent)
                .backgroundColorRes(R.color.primary_dark)
                .positiveColorRes(R.color.accent)
                .negativeColorRes(R.color.accent)
                .widgetColorRes(R.color.accent)
                .callback(new MaterialDialog.ButtonCallback() {

                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public void onPositive(MaterialDialog dialog) {

                        View view = dialog.getCustomView();
                        EditText passwordInput = (EditText) view.findViewById(R.id.passwordInput);
                        if (profile.decrypt(passwordInput.getText().toString())) {
                            changeProfile(profile);
                        } else {
//                            dialog.hide();
//                            mAccountSpinner.setSelection(currentPosition, false);
                        }
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {

//                        dialog.hide();
//                        mAccountSpinner.setSelection(currentPosition, false);
                    }
                })
                .build()
                .show();
    }

    private void initSearch() {

        mSearchTextView.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String searchValue = editable.toString();
                updateAppList(searchValue);
            }
        });

        mSearchTextView.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {

                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    GeneralUtil.hideKeyBoard(mSearchTextView, BaseActivity.this);
                    return true;
                }
                return false;
            }
        });
    }

    protected void updateAppList(String filter) {
        ArrayList<Dapp> dapps = new ArrayList<>(mDApps.size());
        int length = mDApps.size();
        for (int i = 0; i < length; i++) {
            Dapp item = mDApps.get(i);
            if (item.getName().toLowerCase().contains(filter.toLowerCase())) {
                dapps.add(item);
            }
        }
        mDAppsDrawerAdapter = new DAppDrawerAdapter(dapps, this);
        mDAppsRecyclerView.setAdapter(mDAppsDrawerAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_import_wallet:
                onProfileImport();
                break;
            case R.id.ll_contribute:
                String url = CONTRIBUTE_LINK;
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                closeDrawer(DRAWER_CLOSE_DELAY_LONG);
                break;
            case R.id.ll_settings:
                startActivity(new Intent(BaseActivity.this, SettingsActivity.class));
                closeDrawer(DRAWER_CLOSE_DELAY_LONG);
                break;
            case R.id.drawer_header_item:
                flipDrawer();
                break;
        }

    }

    private void showAccountCreateDialog() {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("New account")
                .content("Put your name to create new account")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("Name", "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        Profile profile = new Profile();
                        profile.setName(input.toString());
                        mProfiles.add(profile);
                        PrefsUtil.saveProfile(profile);
                        mProfileDrawerAdapter.notifyDataSetChanged();
                    }
                }).show();
        dialog.getInputEditText().setSingleLine();
    }


    private void flipDrawer() {
        ImageView imageView = (ImageView) findViewById(R.id.drawer_indicator);
        if (isDrawerFrontViewActive()) {
            mFrontView.setVisibility(View.GONE);
            mBackView.setVisibility(VISIBLE);
            imageView.setImageResource(R.drawable.ic_arrow_drop_up_black_24dp);
        } else {
            mBackView.setVisibility(View.GONE);
            mFrontView.setVisibility(VISIBLE);
            imageView.setImageResource(R.drawable.ic_arrow_drop_down_black_24dp);
        }
    }

    private boolean isDrawerFrontViewActive() {
        return mFrontView.getVisibility() == VISIBLE;
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onDAppItemClick(Dapp dapp) {
        onDAppClick(dapp);
        closeDrawer();
    }

    @Override
    public void onProfileClick(Profile profile) {
        changeProfile(profile);
        flipDrawer();
    }

    @Override
    public void onProfileImport() {
        new MaterialDialog.Builder(this)
                .title(R.string.wallet_title)
                .customView(R.layout.wallet_import, true)
                .positiveText(R.string.sImport)
                .negativeText(R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {

                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        RadioButton importJsonRadio = (RadioButton) dialog.findViewById(R.id.radio_import_json);
                        EditText importPathEdit = (EditText) dialog.findViewById(R.id.wallet_import_path);
                        EditText walletPasswordEdit = (EditText) dialog.findViewById(R.id.wallet_password);
                        String importPath = importPathEdit.getText().toString();
                        String password = walletPasswordEdit.getText().toString();
                        String fileContents = null;
                        try {
                            File walletFile = new File(importPath);
                            if (walletFile.exists()) {
                                FileInputStream stream = new FileInputStream(walletFile);
                                try {
                                    FileChannel fileChannel = stream.getChannel();
                                    MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
                                    fileContents = Charset.defaultCharset().decode(buffer).toString();
                                } finally {
                                    stream.close();
                                }
                            } else {
                                Toast.makeText(BaseActivity.this, R.string.file_not_found, Toast.LENGTH_SHORT).show();
                                logger.warn("Wallet file not found: " + importPath);
                                return;
                            }
                        } catch (Exception e) {
                            Toast.makeText(BaseActivity.this, R.string.error_reading_file, Toast.LENGTH_SHORT).show();
                            logger.error("Error reading wallet file", e);
                        }

                        if (importJsonRadio.isChecked()) {
                            if (SyngApplication.sCurrentProfile != null) {
                                if (SyngApplication.sCurrentProfile.importWallet(fileContents, password)) {
                                    PrefsUtil.updateProfile(SyngApplication.sCurrentProfile);
                                    SyngApplication.changeProfile(SyngApplication.sCurrentProfile);
                                } else {
                                    Toast.makeText(BaseActivity.this, R.string.invalid_wallet_password, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                logger.warn("SyngApplication.sCurrentProfile is null ...?!");
                            }
                        } else {
                            SyngApplication.sCurrentProfile.importPrivateKey(fileContents, password);
                        }
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.hide();
                    }
                })
                .build().show();
    }

    @Override
    public void onDAppPress(final Dapp dapp) {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("Edit")
                .customView(R.layout.dapp_form, true)
                .positiveText(R.string.save)
                .negativeText(R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {

                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        EditText dappNameEdit = (EditText) dialog.findViewById(R.id.dapp_name);
                        EditText dappUrlEdit = (EditText) dialog.findViewById(R.id.dapp_url);
                        CheckBox checkBox = (CheckBox) dialog.findViewById(R.id.dapp_home_icon);
                        boolean homeScreenIcon = checkBox.isChecked();
                        String url = dappUrlEdit.getText().toString();
                        String name = dappNameEdit.getText().toString();
                        if (Patterns.WEB_URL.matcher(url.replace("dapp://", "http://")).matches()) {
                            dapp.setName(name);
                            dapp.setUrl(url);
                            System.out.println(url);
                            SyngApplication.updateDapp(dapp);
                            initDApps();
                            if (homeScreenIcon) {
                                GeneralUtil.createHomeScreenIcon(BaseActivity.this, name, url);
                            }
                            dialog.hide();
                        } else {
                            Toast.makeText(BaseActivity.this, R.string.invalid_url, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.hide();
                    }
                })
                .autoDismiss(false)
                .build();
        EditText dappNameEdit = (EditText) dialog.findViewById(R.id.dapp_name);
        dappNameEdit.setText(dapp.getName());
        EditText dappUrlEdit = (EditText) dialog.findViewById(R.id.dapp_url);
        dappUrlEdit.setText(dapp.getUrl());
        dialog.show();
    }

    @Override
    public void onProfilePress(final Profile profile) {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("Edit account")
                .content("Put your name to create new account")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("Name", "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        profile.setName(input.toString());
                        PrefsUtil.updateProfile(profile);
                        for (Profile item : mProfiles) {
                            if (item.getId().equals(profile.getId())) {
                                int index = mProfiles.indexOf(item);
                                mProfiles.set(index, profile);
                                break;
                            }
                        }
                        mProfileDrawerAdapter.notifyDataSetChanged();
                        if (SyngApplication.sCurrentProfile.getId().equals(profile.getId())) {
                            updateCurrentProfileName(profile.getName());
                        }
                    }
                }).show();
        dialog.getInputEditText().setSingleLine();
        dialog.getInputEditText().setText(profile.getName());
    }


    @Override
    public void onDAppAdd() {
        new MaterialDialog.Builder(this)
                .title("Add new one")
                .customView(R.layout.dapp_form, true)
                .positiveText(R.string.save)
                .negativeText(R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {

                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        EditText dappNameEdit = (EditText) dialog.findViewById(R.id.dapp_name);
                        EditText dappUrlEdit = (EditText) dialog.findViewById(R.id.dapp_url);
                        CheckBox checkBox = (CheckBox) dialog.findViewById(R.id.dapp_home_icon);
                        boolean homeScreenIcon = checkBox.isChecked();
                        String url = dappUrlEdit.getText().toString();
                        String name = dappNameEdit.getText().toString();
                        if (Patterns.WEB_URL.matcher(url.replace("dapp://", "http://")).matches()) {
                            Dapp dapp = new Dapp(name);
                            dapp.setUrl(url);
                            SyngApplication.addDapp(dapp);
                            initDApps();
                            if (homeScreenIcon) {
                                GeneralUtil.createHomeScreenIcon(BaseActivity.this, name, url);
                            }
                            dialog.hide();
                        } else {
                            Toast.makeText(BaseActivity.this, R.string.invalid_url, Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.hide();
                    }
                })
                .autoDismiss(false)
                .build().show();
    }

    @Override
    public void onDAppContinueSearch() {
        String url = CONTINUE_SEARCH_LINK;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
            closeDrawer();
            mSearchTextView.getText().clear();
        }
    }

    @Override
    public void onNewProfile() {
        showAccountCreateDialog();
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getId() == R.id.drawer_header_item) {
            new MaterialDialog.Builder(this)
                    .adapter(new BackgroundArrayAdapter(this),
                            new MaterialDialog.ListCallback() {
                                @SuppressWarnings("ConstantConditions")
                                @Override
                                public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                    BackgroundArrayAdapter adapter = (BackgroundArrayAdapter) dialog.getListView().getAdapter();
                                    Glide.with(BaseActivity.this).load(adapter.getImageResourceIdByPosition(which)).into(mHeaderImageView);
                                    dialog.dismiss();
                                }
                            })
                    .show();
        }
        return true;
    }
}
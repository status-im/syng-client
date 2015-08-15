package io.syng.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
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
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import io.syng.entity.Dapp;
import io.syng.entity.Profile;
import io.syng.util.GeneralUtil;
import io.syng.util.PrefsUtil;
import io.syng.util.ProfileManager;

import static android.view.View.VISIBLE;

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
    private ImageView mHeaderImageView;

    protected abstract void onDAppClick(Dapp dapp);

    @SuppressLint("InflateParams")
    @Override
    public void setContentView(final int layoutResID) {
        LayoutInflater inflater = getLayoutInflater();
        mDrawerLayout = (DrawerLayout) inflater.inflate(R.layout.drawer, null, false);

        super.setContentView(mDrawerLayout);

        FrameLayout content = (FrameLayout) findViewById(R.id.content);
        ViewGroup inflated = (ViewGroup) inflater.inflate(layoutResID, content, true);
        Toolbar toolbar = (Toolbar) inflated.findViewById(R.id.myToolbar);

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
        populateProfiles();

        mDAppsRecyclerView = (RecyclerView) findViewById(R.id.dapd_drawer_recycler_view);
        mDAppsRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(this);
        mDAppsRecyclerView.setLayoutManager(layoutManager1);
        populateDApps();

        mHeaderImageView = (ImageView) findViewById(R.id.iv_header);
        String currentProfileId = ProfileManager.getCurrentProfile().getId();
        Glide.with(this).load(PrefsUtil.getBackgroundResourceId(currentProfileId)).into(mHeaderImageView);

        GeneralUtil.showWarningDialogIfNeed(this);
    }

    private void populateProfiles() {
        mProfileDrawerAdapter = new ProfileDrawerAdapter(this, ProfileManager.getProfiles(), this);
        mProfilesRecyclerView.setAdapter(mProfileDrawerAdapter);
        updateCurrentProfileName();
    }

    private void populateDApps() {
        updateDAppList(mSearchTextView.getText().toString());
    }

    private void closeDrawer(int delayMills) {
        mHandler.postDelayed(mRunnable, delayMills);
    }

    protected void closeDrawer() {
        mHandler.postDelayed(mRunnable, DRAWER_CLOSE_DELAY_SHORT);
    }

    private void changeProfile(Profile profile) {
        ProfileManager.setCurrentProfile(profile);
        updateCurrentProfileName();
        Glide.with(this).load(PrefsUtil.getBackgroundResourceId(profile.getId())).into(mHeaderImageView);
        populateDApps();
        flipDrawer();
    }

    private void updateCurrentProfileName() {
        TextView textView = (TextView) findViewById(R.id.tv_name);
        textView.setText(ProfileManager.getCurrentProfile().getName());
    }

    private void requestChangeProfile(final Profile profile) {
        Dialog dialog = new MaterialDialog.Builder(BaseActivity.this)
                .title(R.string.request_profile_password)
                .customView(R.layout.profile_password, true)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        View view = dialog.getCustomView();
                        EditText password = (EditText) view.findViewById(R.id.et_pass);
                        if (profile.checkPassword(password.getText().toString())) {
                            changeProfile(profile);
                        } else {
                            Toast.makeText(BaseActivity.this, "Password is not correct", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
        EditText name = (EditText) dialog.findViewById(R.id.et_pass);
        GeneralUtil.showKeyBoard(name, this);
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
                updateDAppList(searchValue);
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

    private void updateDAppList(String filter) {
        List<Dapp> mDApps = ProfileManager.getCurrentProfile().getDapps();
        ArrayList<Dapp> dapps = new ArrayList<>(mDApps.size());
        int length = mDApps.size();
        for (int i = 0; i < length; i++) {
            Dapp item = mDApps.get(i);
            if (item.getName().toLowerCase().contains(filter.toLowerCase())) {
                dapps.add(item);
            }
        }
        DAppDrawerAdapter mDAppsDrawerAdapter = new DAppDrawerAdapter(dapps, this);
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

    @SuppressWarnings("ConstantConditions")
    private void showProfileCreateDialog() {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("New account")
                .positiveText(R.string.dialog_button_create)
                .negativeText(R.string.dialog_button_cancel)
                .customView(R.layout.profile_create_dialog, true)
                .autoDismiss(false)
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
                            Toast.makeText(BaseActivity.this, "Profile name can't be empty", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (TextUtils.isEmpty(pass1String) || TextUtils.isEmpty(pass2String)) {
                            Toast.makeText(BaseActivity.this, "Password name can't be empty", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (!pass1.getText().toString().equals(pass2.getText().toString())) {
                            Toast.makeText(BaseActivity.this, "Passwords should be the same!", Toast.LENGTH_SHORT).show();
                        } else {
                            Profile profile = new Profile();
                            profile.setName(name.getText().toString());
                            profile.setPassword(pass1String);
                            ProfileManager.addProfile(profile);
                            mProfileDrawerAdapter.swapData(ProfileManager.getProfiles());
                            GeneralUtil.hideKeyBoard(name, BaseActivity.this);
                            GeneralUtil.hideKeyBoard(pass1, BaseActivity.this);
                            GeneralUtil.hideKeyBoard(pass2, BaseActivity.this);
                            dialog.dismiss();
                        }
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.dismiss();
                    }
                }).show();
        EditText name = (EditText) dialog.findViewById(R.id.et_profile_name);
        GeneralUtil.showKeyBoard(name, this);
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
        if (ProfileManager.getCurrentProfile().getId().equals(profile.getId())) {
            return;
        }
        requestChangeProfile(profile);
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
                            Profile profile = ProfileManager.getCurrentProfile();
                            if (profile.importWallet(fileContents, password)) {
                                ProfileManager.updateProfile(profile);
                                ProfileManager.setCurrentProfile(profile);
                            } else {
                                Toast.makeText(BaseActivity.this, R.string.invalid_wallet_password, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Profile profile = ProfileManager.getCurrentProfile();
                            profile.importPrivateKey(fileContents, password);
                            ProfileManager.updateProfile(profile);
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
                            ProfileManager.updateDAppInProfile(ProfileManager.getCurrentProfile(), dapp);
                            populateDApps();
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

    @SuppressWarnings("ConstantConditions")
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
                        ProfileManager.updateProfile(profile);
                        mProfileDrawerAdapter.swapData(ProfileManager.getProfiles());
                        updateCurrentProfileName();
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
                            ProfileManager.addDAppToProfile(ProfileManager.getCurrentProfile(), dapp);
                            populateDApps();
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
        showProfileCreateDialog();
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
                                    int imageResourceId = adapter.getImageResourceIdByPosition(which);
                                    Glide.with(BaseActivity.this).load(imageResourceId).into(mHeaderImageView);
                                    PrefsUtil.setBackgroundResourceId(ProfileManager.getCurrentProfile().getId(), imageResourceId);
                                    dialog.dismiss();
                                }
                            })
                    .show();
        }
        return true;
    }
}
package io.syng.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
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
import io.syng.adapter.AccountDrawerAdapter;
import io.syng.adapter.AccountDrawerAdapter.OnProfileClickListener;
import io.syng.adapter.DAppDrawerAdapter;
import io.syng.adapter.DAppDrawerAdapter.OnDAppClickListener;
import io.syng.app.SyngApplication;
import io.syng.entity.Dapp;
import io.syng.entity.Profile;
import io.syng.util.GeneralUtil;
import io.syng.util.PrefsUtil;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.ethereum.config.SystemProperties.CONFIG;

public abstract class BaseActivity extends AppCompatActivity implements
        OnClickListener, OnDAppClickListener, OnProfileClickListener {

    private static final Logger logger = LoggerFactory.getLogger("SyngApplication");

    private static final int DRAWER_CLOSE_DELAY_SHORT = 200;
    private static final int DRAWER_CLOSE_DELAY_LONG = 400;

    private static final String CONTRIBUTE_LINK = "https://github.com/syng-io";
    private static final String CONTINUE_SEARCH_LINK = "dapp://syng.io/store?q=search%20query";

    private ActionBarDrawerToggle mDrawerToggle;

    private Spinner mAccountSpinner;
    private EditText mSearchTextView;
    private RecyclerView mDAppsRecyclerView;
    private RecyclerView mAccountsRecyclerView;
    private DrawerLayout mDrawerLayout;

    private DAppDrawerAdapter mDAppsDrawerAdapter;
    private AccountDrawerAdapter mAccountDrawerAdapter;

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

    protected abstract void onDAppClick(Dapp dapp);

    private SpinnerAdapter spinnerAdapter;
    private Profile requestProfile;

    @SuppressLint("InflateParams")
    @Override
    public void setContentView(final int layoutResID) {
        LayoutInflater inflater = getLayoutInflater();
        mDrawerLayout = (DrawerLayout) inflater.inflate(R.layout.drawer, null, false);
        FrameLayout content = (FrameLayout) mDrawerLayout.findViewById(R.id.content);

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

        mSearchTextView = (EditText) mDrawerLayout.findViewById(R.id.search);
        initSearch();

        mDrawerLayout.findViewById(R.id.ll_import_wallet).setOnClickListener(this);
        mDrawerLayout.findViewById(R.id.ll_settings).setOnClickListener(this);
        mDrawerLayout.findViewById(R.id.ll_contribute).setOnClickListener(this);
        mDrawerLayout.findViewById(R.id.drawer_header).setOnClickListener(this);

        mFrontView = mDrawerLayout.findViewById(R.id.ll_front_view);
        mBackView = mDrawerLayout.findViewById(R.id.ll_back_view);
        mBackView.setVisibility(GONE);

        mAccountsRecyclerView = (RecyclerView) mBackView.findViewById(R.id.accounts_drawer_recycler_view);
        RecyclerView.LayoutManager layoutManager2 = new LinearLayoutManager(this);
        mAccountsRecyclerView.setLayoutManager(layoutManager2);
        initProfiles();

        mDAppsRecyclerView = (RecyclerView) mDrawerLayout.findViewById(R.id.dapd_drawer_recycler_view);
        mDAppsRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(this);
        mDAppsRecyclerView.setLayoutManager(layoutManager1);
        initDApps();

        ImageView header = (ImageView) mDrawerLayout.findViewById(R.id.iv_header);
        Glide.with(this).load(R.drawable.two).into(header);

        super.setContentView(mDrawerLayout);
        TextView textView = (TextView) findViewById(R.id.tv_name);
        if (textView != null) {
            textView.setText("Cow");
        }

        showWarningDialogIfNeed();

    }

    private void showWarningDialogIfNeed() {
        if (PrefsUtil.isFirstLaunch()) {
            PrefsUtil.setFirstLaunch(false);
            new AlertDialogWrapper.Builder(this)
                    .setTitle(R.string.warning_title)
                    .setMessage(R.string.warning_message)
                    .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        }
    }


    private void initProfiles() {
        mProfiles = PrefsUtil.getProfiles();
        // Add default cow account if not present
        if (mProfiles.size() == 0) {
            Profile profile = new Profile();
            profile.setName("Cow");
            // Add default cow and monkey addresses
            List<String> addresses = new ArrayList<String>();
            byte[] cowAddr = HashUtil.sha3("cow".getBytes());
            addresses.add(Hex.toHexString(cowAddr));
            String secret = CONFIG.coinbaseSecret();
            byte[] cbAddr = HashUtil.sha3(secret.getBytes());
            addresses.add(Hex.toHexString(cbAddr));
            profile.setPrivateKeys(addresses);
            PrefsUtil.saveProfile(profile);
            mProfiles.add(profile);
        }
        mAccountDrawerAdapter = new AccountDrawerAdapter(this, mProfiles, this);
        mAccountsRecyclerView.setAdapter(mAccountDrawerAdapter);
        if (SyngApplication.currentProfile == null) {
            SyngApplication.changeProfile(mProfiles.get(0));
        }
    }


    private void initDApps() {
        mDApps = new ArrayList<>();
        if (SyngApplication.currentProfile != null) {
            mDApps = SyngApplication.currentProfile.getDapps();
        }
        updateAppList(mSearchTextView.getText().toString());
    }


    private void closeDrawer(int delayMills) {
        mHandler.postDelayed(mRunnable, delayMills);
    }

    protected void changeProfile(Profile profile) {

        TextView textView = (TextView) findViewById(R.id.tv_name);
        if (textView != null) {
            textView.setText(profile.getName());
        }
        SyngApplication.changeProfile(profile);
        initDApps();
    }

    protected void requestChangeProfile(Profile profile) {

        requestProfile = profile;
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

                    @Override
                    public void onPositive(MaterialDialog dialog) {

                        View view = dialog.getCustomView();
                        EditText passwordInput = (EditText) view.findViewById(R.id.passwordInput);
                        if (requestProfile.decrypt(passwordInput.getText().toString())) {
                            changeProfile(requestProfile);
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

    public void initSpinner() {

        List<Profile> profilesList = PrefsUtil.getProfiles();
        spinnerAdapter = new SpinnerAdapter(this, android.R.layout.simple_dropdown_item_1line, profilesList);
//        mAccountSpinner.setAdapter(spinnerAdapter);
//        mAccountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                //String item = (String) adapterView.getItemAtPosition(i);
//                if (adapterView != null && adapterView.getChildAt(0) != null) {
//                    ((TextView) adapterView.getChildAt(0)).setTextColor(Color.parseColor("#ffffff"));
//                }
//                Profile profile = spinnerAdapter.getItem(i);
//                if (profile.getPasswordProtectedProfile()) {
//                    requestChangeProfile(profile);
//                } else {
//                    changeProfile(profile);
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//                ((TextView) adapterView.getChildAt(0)).setTextColor(Color.parseColor("#ffffff"));
//            }
//
//        });
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
            case R.id.drawer_header:
                flipDrawer();
                break;
        }

    }

    private void showAccountCreateDialog() {
        new MaterialDialog.Builder(this)
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
                        mAccountDrawerAdapter.notifyDataSetChanged();
                    }
                }).show();
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


    public class SpinnerAdapter extends ArrayAdapter<Profile> {

        private Context context;

        private List<Profile> values;

        public SpinnerAdapter(Context context, int textViewResourceId, List<Profile> values) {

            super(context, textViewResourceId, values);
            this.context = context;
            this.values = values;
        }

        public int getCount() {

            return values.size();
        }

        public Profile getItem(int position) {

            return values.get(position);
        }

        public long getItemId(int position) {

            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {

            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = getLayoutInflater();
            View row = inflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
            TextView label = (TextView) row.findViewById(android.R.id.text1);
            label.setText(spinnerAdapter.getItem(position).getName());
            return row;
        }
    }

    @Override
    public void onDAppItemClick(Dapp dapp) {
        onDAppClick(dapp);
        closeDrawer(DRAWER_CLOSE_DELAY_SHORT);
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
                        RadioButton importJsonRadio = (RadioButton)dialog.findViewById(R.id.radio_import_json);
                        EditText importPathEdit = (EditText)dialog.findViewById(R.id.wallet_import_path);
                        EditText walletPasswordEdit = (EditText)dialog.findViewById(R.id.wallet_password);
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
                            if (SyngApplication.currentProfile != null) {
                                if (SyngApplication.currentProfile.importWallet(fileContents, password)) {
                                    PrefsUtil.updateProfile(SyngApplication.currentProfile);
                                    SyngApplication.changeProfile(SyngApplication.currentProfile);
                                } else {
                                    Toast.makeText(BaseActivity.this, R.string.invalid_wallet_password, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                logger.warn("SyngApplication.currentProfile is null ...?!");
                            }
                        } else {
                            SyngApplication.currentProfile.importPrivateKey(fileContents, password);
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
                        dapp.setName(dappNameEdit.getText().toString());
                        dapp.setUrl(dappUrlEdit.getText().toString());
                        SyngApplication.updateDapp(dapp);
                        initDApps();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.hide();
                    }
                })
                .build();
        EditText dappNameEdit = (EditText) dialog.findViewById(R.id.dapp_name);
        dappNameEdit.setText(dapp.getName());
        EditText dappUrlEdit = (EditText) dialog.findViewById(R.id.dapp_url);
        dappUrlEdit.setText(dapp.getUrl());
        dialog.show();
    }

    @Override
    public void onProfilePress(Profile profile) {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("Edit account")
                .content("Put your name to create new account")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("Name", "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        Toast.makeText(BaseActivity.this, "Just do nothing", Toast.LENGTH_SHORT).show();
                    }
                }).show();
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
                        EditText dappNameEdit = (EditText)dialog.findViewById(R.id.dapp_name);
                        EditText dappUrlEdit = (EditText)dialog.findViewById(R.id.dapp_url);
                        Dapp dapp = new Dapp(dappNameEdit.getText().toString());
                        dapp.setUrl(dappUrlEdit.getText().toString());
                        SyngApplication.addDapp(dapp);
                        initDApps();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.hide();
                    }
                })
                .build().show();
    }

    @Override
    public void onDAppContinueSearch() {
        String url = CONTINUE_SEARCH_LINK;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
            closeDrawer(DRAWER_CLOSE_DELAY_SHORT);
            mSearchTextView.getText().clear();
        }
    }

    @Override
    public void onNewProfile() {
        showAccountCreateDialog();
    }
}
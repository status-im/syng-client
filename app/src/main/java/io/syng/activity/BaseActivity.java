package io.syng.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import io.syng.R;
import io.syng.adapter.DAppDrawerAdapter;
import io.syng.adapter.DAppDrawerAdapter.OnDAppClickListener;
import io.syng.adapter.ProfileDrawerAdapter;
import io.syng.adapter.ProfileDrawerAdapter.OnProfileClickListener;
import io.syng.adapter.helper.SimpleItemTouchHelperCallback;
import io.syng.entity.Dapp;
import io.syng.entity.Profile;
import io.syng.fragment.profile.ProfileDialogFragment;
import io.syng.util.GeneralUtil;
import io.syng.util.ProfileManager;
import io.syng.util.ProfileManager.ProfilesChangeListener;

import static android.view.View.VISIBLE;

public abstract class BaseActivity extends AppCompatActivity implements
        OnClickListener, OnDAppClickListener, OnProfileClickListener, OnLongClickListener, ProfilesChangeListener {

    private static final int DRAWER_CLOSE_DELAY_SHORT = 200;
    private static final int DRAWER_CLOSE_DELAY_LONG = 400;

    private static final String CONTRIBUTE_LINK = "https://github.com/syng-io";
    private static final String CONTINUE_SEARCH_LINK = "dapp://syng.io/store?q=search%20query";

    private ActionBarDrawerToggle mDrawerToggle;

    private EditText mSearchTextView;
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
    private DAppDrawerAdapter mDAppsDrawerAdapter;

    protected abstract void onDAppClick(Dapp dapp);

    private ItemTouchHelper mItemTouchHelper;

    @SuppressLint("InflateParams")
    @Override
    public void setContentView(final int layoutResID) {
        LayoutInflater inflater = getLayoutInflater();
        mDrawerLayout = (DrawerLayout) inflater.inflate(R.layout.drawer, null, false);

        super.setContentView(mDrawerLayout);

        FrameLayout content = (FrameLayout) findViewById(R.id.content);
        ViewGroup inflated = (ViewGroup) inflater.inflate(layoutResID, content, true);
        Toolbar toolbar = (Toolbar) inflated.findViewById(R.id.app_toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            mDrawerLayout.setStatusBarBackgroundColor(ContextCompat.getColor(this, android.R.color.black));
        }

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar,
                R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                GeneralUtil.hideKeyBoard(mSearchTextView, BaseActivity.this);
                mDAppsDrawerAdapter.setEditModeEnabled(false);
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

        RecyclerView profilesRecyclerView = (RecyclerView) findViewById(R.id.profile_drawer_recycler_view);
        RecyclerView.LayoutManager layoutManager2 = new LinearLayoutManager(this);
        profilesRecyclerView.setLayoutManager(layoutManager2);
        mProfileDrawerAdapter = new ProfileDrawerAdapter(this, new ArrayList<Profile>(), this);
        profilesRecyclerView.setAdapter(mProfileDrawerAdapter);
        updateCurrentProfileName();
        populateProfiles();

        RecyclerView dAppsRecyclerView = (RecyclerView) findViewById(R.id.dapp_drawer_recycler_view);
        dAppsRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(this);
        dAppsRecyclerView.setLayoutManager(layoutManager1);
        mDAppsDrawerAdapter = new DAppDrawerAdapter(this, this, new DAppDrawerAdapter.OnStartDragListener() {
            @Override
            public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
                mItemTouchHelper.startDrag(viewHolder);
            }
        });
        dAppsRecyclerView.setAdapter(mDAppsDrawerAdapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mDAppsDrawerAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(dAppsRecyclerView);

        populateDApps();

        mHeaderImageView = (ImageView) findViewById(R.id.iv_header);
        Glide.with(this).load(ProfileManager.getCurrentProfileBackgroundResourceId()).into(mHeaderImageView);

        GeneralUtil.showWarningDialogIfNeed(this);
        ProfileManager.setProfilesChangeListener(this);
    }


    private void populateProfiles() {
        mProfileDrawerAdapter.swapData(ProfileManager.getProfiles());
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
                            ProfileManager.setCurrentProfile(profile);
                            flipDrawer();
                        } else {
                            Toast.makeText(BaseActivity.this, "Password is not correct", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
        EditText name = (EditText) dialog.findViewById(R.id.et_pass);
        GeneralUtil.showKeyBoard(name, this);
    }

    private void updateCurrentProfileName() {
        TextView textView = (TextView) findViewById(R.id.tv_name);
        textView.setText(ProfileManager.getCurrentProfile().getName());
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
        mDAppsDrawerAdapter.setEditModeEnabled(false);
        mDAppsDrawerAdapter.swapData(dapps);
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
        ProfileManager.removeProfilesChangeListener(this);
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
                if (isDrawerFrontViewActive()) {
                    mDAppsDrawerAdapter.setEditModeEnabled(false);
                    GeneralUtil.hideKeyBoard(mSearchTextView, BaseActivity.this);
                }
                flipDrawer();
                break;
        }
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
        if (!ProfileManager.getCurrentProfile().getId().equals(profile.getId())) {
            requestChangeProfile(profile);
        }
    }

    @Override
    public void onProfileImport() {
        GeneralUtil.showProfileImportDialog(this);
    }


    @Override
    public void onDAppEdit(final Dapp dapp) {
        GeneralUtil.showDAppEditDialog(dapp, this);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onProfilePress(final Profile profile) {
        if (ProfileManager.getCurrentProfile().getId().equals(profile.getId())) {
            ProfileDialogFragment dialogFragment = ProfileDialogFragment.newInstance(profile);
            dialogFragment.show(getSupportFragmentManager(), "profile_dialog");
        } else {
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
                                ProfileDialogFragment dialogFragment = ProfileDialogFragment.newInstance(profile);
                                dialogFragment.show(getSupportFragmentManager(), "profile_dialog");
                            } else {
                                Toast.makeText(BaseActivity.this, "Password is not correct", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .show();
            EditText name = (EditText) dialog.findViewById(R.id.et_pass);
            GeneralUtil.showKeyBoard(name, this);
        }

    }

    @Override
    public void onDAppAdd() {
        GeneralUtil.showDAppCreateDialog(this);
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
        GeneralUtil.showProfileCreateDialog(this);
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getId() == R.id.drawer_header_item) {
            GeneralUtil.showHeaderBackgroundDialog(this);
        }
        return true;
    }

    @Override
    public void onProfilesChange() {
        updateCurrentProfileName();
        populateDApps();
        populateProfiles();
        Glide.with(this).load(ProfileManager.getCurrentProfileBackgroundResourceId()).into(mHeaderImageView);
    }

}
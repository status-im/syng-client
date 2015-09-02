/*
 * Copyright (c) 2015 Jarrad Hope
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.syng.fragment.profile;

import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;

import io.syng.R;
import io.syng.adapter.ProfileViewPagerAdapter;
import io.syng.entity.Profile;
import io.syng.util.GeneralUtil;

public class ProfileDialogFragment extends DialogFragment implements OnPageChangeListener,
        OnClickListener {

    private static final String ARG_PROFILE_ID = "profile";

    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private Toolbar mToolbar;
    private String mProfileId;
    private MenuItem importProfile;
    private MenuItem exportProfile;

    public static ProfileDialogFragment newInstance(final Profile profile) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_PROFILE_ID, profile.getId());
        ProfileDialogFragment dialogFragment = new ProfileDialogFragment();
        dialogFragment.setArguments(bundle);
        return dialogFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mProfileId = getArguments().getString(ARG_PROFILE_ID);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_edit_dialog, container);
        mTabLayout = (TabLayout) view.findViewById(R.id.profile_tabs);
        mViewPager = (ViewPager) view.findViewById(R.id.profile_view_pager);

        mViewPager.addOnPageChangeListener(this);
        mToolbar = (Toolbar) view.findViewById(R.id.profile_toolbar);
        mToolbar.setTitle("Edit Profile");
        mToolbar.inflateMenu(R.menu.profile_menu);

        exportProfile = mToolbar.getMenu().findItem(R.id.action_key_export);
        exportProfile.setVisible(false);
        exportProfile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                return false;
            }
        });
        importProfile = mToolbar.getMenu().findItem(R.id.action_key_import);
        importProfile.setVisible(false);
        importProfile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                GeneralUtil.showProfileImportDialog(getActivity());
                return false;
            }
        });

        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        mToolbar.setNavigationOnClickListener(this);
        tintMenuItem();

        mViewPager.setAdapter(new ProfileViewPagerAdapter(getChildFragmentManager(), getActivity(), mProfileId));
        mTabLayout.setupWithViewPager(mViewPager);

        return view;
    }

    private void tintMenuItem() {
        Drawable drawable1 = mToolbar.getMenu().findItem(R.id.action_key_export).getIcon();
        drawable1 = DrawableCompat.wrap(drawable1);
        DrawableCompat.setTint(drawable1, getResources().getColor(R.color.drawer_icon_color));
        mToolbar.getMenu().findItem(R.id.action_key_export).setIcon(drawable1);

        Drawable drawable2 = mToolbar.getMenu().findItem(R.id.action_key_import).getIcon();
        drawable2 = DrawableCompat.wrap(drawable2);
        DrawableCompat.setTint(drawable2, getResources().getColor(R.color.drawer_icon_color));
        mToolbar.getMenu().findItem(R.id.action_key_import).setIcon(drawable2);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        invalidateToolbarMenu();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    private void invalidateToolbarMenu() {
        MenuItem exp = mToolbar.getMenu().findItem(R.id.action_key_export);
        MenuItem imp = mToolbar.getMenu().findItem(R.id.action_key_import);
        boolean showExport = mViewPager.getCurrentItem() == ProfileViewPagerAdapter.KEYS_POSITION;
        exp.setVisible(showExport);
        imp.setVisible(showExport);
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }
}

package io.syng.fragment;

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
import android.view.ViewGroup;
import android.view.Window;

import io.syng.R;
import io.syng.adapter.ProfileViewPagerAdapter;

public class ProfileDialogFragment extends DialogFragment implements OnPageChangeListener {

    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private Toolbar mToolbar;

    public static ProfileDialogFragment newInstance() {
        return new ProfileDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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
        mToolbar.getMenu().findItem(R.id.action_key_export).setVisible(false);
        mToolbar.getMenu().findItem(R.id.action_key_import).setVisible(false);
        tintMenuItem();

        mViewPager.setAdapter(new ProfileViewPagerAdapter(getChildFragmentManager(), getActivity()));
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
}

package io.syng.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.view.ViewGroup;

import io.syng.fragment.profile.ProfileGeneralFragment;
import io.syng.fragment.profile.ProfileKeysFragment;

public final class ProfileViewPagerAdapter extends FragmentPagerAdapter {

    public static final int GENERAL_POSITION = 0;
    public static final int KEYS_POSITION = 1;

    public static final String[] LABELS = new String[]{"General", "Keys"};
    private final Context mContext;
    private final String mProfileId;

    public ProfileViewPagerAdapter(FragmentManager fragmentManager, Context context, String profileId) {
        super(fragmentManager);
        this.mContext = context;
        mProfileId = profileId;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case GENERAL_POSITION:
                return ProfileGeneralFragment.newInstance(mProfileId);
            case KEYS_POSITION:
                return ProfileKeysFragment.newInstance(mProfileId);
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        FragmentManager manager = ((Fragment) object).getFragmentManager();
        FragmentTransaction trans = manager.beginTransaction();
        trans.remove((Fragment) object);
        trans.commit();
        super.destroyItem(container, position, object);
    }

    @Override
    public int getCount() {
        return LABELS.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return LABELS[position];
    }
}
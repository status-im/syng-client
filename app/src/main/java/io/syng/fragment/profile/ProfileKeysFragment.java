package io.syng.fragment.profile;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.syng.R;
import io.syng.adapter.ProfileKeyAdapter;
import io.syng.entity.Profile;
import io.syng.util.ProfileManager;

public class ProfileKeysFragment extends Fragment {

    private static final String ARG_PROFILE_ID = "profile_id";
    private String mProfileId;

    private RecyclerView mRecyclerView;
    private ProfileKeyAdapter mProfileDrawerAdapter;

    public static ProfileKeysFragment newInstance(String profileId) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_PROFILE_ID, profileId);
        ProfileKeysFragment dialogFragment = new ProfileKeysFragment();
        dialogFragment.setArguments(bundle);
        return dialogFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProfileId = getArguments().getString(ARG_PROFILE_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_keys, container, false);

        Profile profile = ProfileManager.getProfileById(mProfileId);
        if (profile != null) {
            profile.getPrivateKeys();
        }

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_profile_keys);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mProfileDrawerAdapter = new ProfileKeyAdapter(profile.getAddresses());
        mRecyclerView.setAdapter(mProfileDrawerAdapter);

        return view;
    }

}

package io.syng.fragment.profile;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import io.syng.R;
import io.syng.entity.Profile;
import io.syng.util.ProfileManager;

public class ProfileGeneralFragment extends Fragment implements TextWatcher {

    private static final String ARG_PROFILE_ID = "profile_id";
    private String mProfileId;

    public static ProfileGeneralFragment newInstance(String profileId) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_PROFILE_ID, profileId);
        ProfileGeneralFragment dialogFragment = new ProfileGeneralFragment();
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
        View view = inflater.inflate(R.layout.fragment_profile_general, container, false);
        EditText profileName = (EditText) view.findViewById(R.id.et_profile_name);
        profileName.addTextChangedListener(this);
        Profile profile = ProfileManager.getProfileById(mProfileId);
        if (profile != null) {
            profileName.setText(profile.getName());
        }
        return view;
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        Profile profile = ProfileManager.getProfileById(mProfileId);
        if (profile != null) {
            profile.setName(s.toString());
            ProfileManager.updateProfile(profile);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

}

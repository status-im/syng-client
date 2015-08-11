package io.syng.fragment;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import co.dift.ui.SwipeToAction;
import io.syng.R;
import io.syng.activity.BaseActivity;
import io.syng.activity.ProfileManagerActivity;
import io.syng.adapter.ProfileAdapter;
import io.syng.entity.Profile;
import io.syng.interfaces.OnFragmentInteractionListener;
import io.syng.util.PrefsUtil;


/**
 * A placeholder fragment containing a simple view.
 */
public class ProfileManagerFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProfileAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private ArrayList<Profile> profiles = new ArrayList<>();

    private SwipeToAction swipeToAction;

    private OnFragmentInteractionListener mListener;

    public int profileEditPosition = -1;

    public static ProfileManagerFragment newInstance(String param1, String param2) {

        ProfileManagerFragment fragment = new ProfileManagerFragment();
        return fragment;
    }

    public ProfileManagerFragment() {

    }

    public void addProfile(Profile profile) {

        if (profileEditPosition > -1) {
            adapter.set(profileEditPosition, profile);
        } else {
            adapter.add(profile);
        }
        updateProfiles();
    }

    public void resetProfilePosition() {
        profileEditPosition = -1;
    }

    public void updateProfiles() {

        BaseActivity activity = (BaseActivity)getActivity();
        PrefsUtil.saveProfiles(adapter.getItems());
        activity.initSpinner();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile_manager, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.profile_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)

        profiles = PrefsUtil.getProfiles();
        adapter = new ProfileAdapter(profiles);
        recyclerView.setAdapter(adapter);

        swipeToAction = new SwipeToAction(recyclerView, new SwipeToAction.SwipeListener<Profile>() {
            @Override
            public boolean swipeLeft(final Profile itemData) {

                adapter.remove(itemData);
                updateProfiles();
                return false; //true will move the front view to its starting position
            }

            @Override
            public boolean swipeRight(Profile itemData) {
                //do something
                return true;
            }

            @Override
            public void onClick(Profile itemData) {

                profileEditPosition = adapter.getPosition(itemData);
                ProfileManagerActivity activity = (ProfileManagerActivity)getActivity();
                activity.showAddProfile(itemData);
            }

            @Override
            public void onLongClick(Profile itemData) {
                //do something
            }
        });

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {

        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {

        super.onDetach();
        mListener = null;
    }


}

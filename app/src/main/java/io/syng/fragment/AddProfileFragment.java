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
import android.widget.EditText;
import android.widget.Switch;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

import co.dift.ui.SwipeToAction;
import io.syng.R;
import io.syng.adapter.DappAdapter;
import io.syng.entity.Dapp;
import io.syng.entity.Profile;
import io.syng.interfaces.OnFragmentInteractionListener;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class AddProfileFragment extends Fragment {

    private EditText profileName;
    private Switch profilePasswordProtected;

    private RecyclerView mDappsRecyclerView;
    private DappAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeToAction swipeToAction;
    private ArrayList<Dapp> mDappsList = new ArrayList<>();

    private OnFragmentInteractionListener mListener;

    private MaterialDialog dappDialog;
    private EditText dappName;
    private EditText dappUrl;

    private Dapp addDapp = new Dapp("new_app_id", "Add new dapp");

    private int dapEditPosition = -1;

    public void setProfile(Profile profile) {

        profileName.setText(profile != null ? profile.getName() : "");
        profilePasswordProtected.setChecked(profile != null ? profile.getPasswordProtectedProfile() : false);

        resetDapps();
        if (profile != null) {
            for (Dapp dapp : profile.getDapps()) {
                mAdapter.add(dapp);
            }
        }
    }

    protected void resetDapps() {
        mAdapter.clear();
        mAdapter.add(addDapp);
    }

    public Profile getProfile() {
        Profile profile = new Profile();
        profile.setName(profileName.getText().toString());
        profile.setPasswordProtectedProfile(profilePasswordProtected.isChecked());
        List<Dapp> dapps = mAdapter.getItems();
        dapps.remove(addDapp);
        profile.setDapps(dapps);
        return profile;
    }

    protected void editDapp(Dapp dapp) {
        dapEditPosition = mAdapter.getPosition(dapp);
        dappName.setText(dapp.getName());
        dappUrl.setText(dapp.getUrl());
        dappDialog.show();
    }

    protected void createDapp() {
        dapEditPosition = -1;
        Dapp dapp = new Dapp();
        dappName.setText(dapp.getName());
        dappUrl.setText(dapp.getUrl());
        dappDialog.show();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_profile, container, false);

        profileName = (EditText) view.findViewById(R.id.profile_name);
        profilePasswordProtected = (Switch) view.findViewById(R.id.profile_password_protected);

        mDappsRecyclerView = (RecyclerView) view.findViewById(R.id.profile_dapps_list);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mDappsRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mDappsRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new DappAdapter(mDappsList);
        resetDapps();
        mDappsRecyclerView.setAdapter(mAdapter);

        swipeToAction = new SwipeToAction(mDappsRecyclerView, new SwipeToAction.SwipeListener<Dapp>() {
            @Override
            public boolean swipeLeft(final Dapp itemData) {
                mAdapter.remove(itemData);
                return false; //true will move the front view to its starting position
            }

            @Override
            public boolean swipeRight(Dapp itemData) {
                return true;
            }

            @Override
            public void onClick(Dapp itemData) {
                if (itemData.getId() == "new_app_id") {
                    createDapp();
                } else {
                    editDapp(itemData);
                }
            }

            @Override
            public void onLongClick(Dapp itemData) {

            }
        });

        boolean wrapInScrollView = true;
        dappDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.dapp_dialog_title)
                .customView(R.layout.dapp_form, wrapInScrollView)
                .positiveText(R.string.save)
                .negativeText(R.string.cancel)
                .contentColor(getResources().getColor(R.color.accent)) // notice no 'res' postfix for literal color
                .dividerColorRes(R.color.accent)
                .backgroundColorRes(R.color.primary_dark)
                .positiveColorRes(R.color.accent)
                .negativeColorRes(R.color.accent)
                .widgetColorRes(R.color.accent)
                .callback(new MaterialDialog.ButtonCallback() {

                    @Override
                    public void onPositive(MaterialDialog dialog) {

                        View view = dialog.getCustomView();
                        Dapp dapp = new Dapp();
                        dapp.setName(dappName.getText().toString());
                        dapp.setUrl(dappUrl.getText().toString());
                        if (dapEditPosition > -1) {
                            mAdapter.set(dapEditPosition, dapp);
                        } else {
                            mAdapter.add(dapp);
                        }
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.hide();
                    }
                })
                .build();
        dappName = (EditText) dappDialog.findViewById(R.id.dapp_name);
        dappUrl = (EditText) dappDialog.findViewById(R.id.dapp_url);

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

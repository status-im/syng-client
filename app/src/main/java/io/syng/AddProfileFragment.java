package io.syng;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.dift.ui.SwipeToAction;
import io.syng.entities.Dapp;
import io.syng.entities.DappAdapter;
import io.syng.entities.Profile;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddProfileFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private EditText profileName;
    private Switch profilePasswordProtected;

    private RecyclerView dappsList;
    private DappAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private SwipeToAction swipeToAction;
    private ArrayList<Dapp> dapps = new ArrayList<>();

    private OnFragmentInteractionListener mListener;

    private MaterialDialog dappDialog;
    private EditText dappName;
    private EditText dappUrl;

    private TextView accountModeButton;
    private MaterialDialog accountModeDialog;
    private RadioButton accountModeCreate;
    private RadioButton accountModeImportFile;
    private RadioButton accountModeImportString;
    private EditText accountImportSource;

    private int accountMode;
    private String accountPrivateKey = null;

    private Dapp addDapp = new Dapp("new_app_id", "Add new dapp");

    protected int dapEditPosition = -1;

    protected View.OnClickListener accountModeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            accountMode = view.getId();
            switch(accountMode) {
                case R.id.radio_new_account:
                    accountImportSource.setInputType(InputType.TYPE_NULL);
                    break;
                case R.id.radio_import_file:
                case R.id.radio_import_string:
                    accountImportSource.setInputType(InputType.TYPE_CLASS_TEXT);
                    break;
            }
        }
    };

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddProfileFragment.
     */
    public static AddProfileFragment newInstance(String param1, String param2) {

        AddProfileFragment fragment = new AddProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public AddProfileFragment() {

        // Required empty public constructor
    }

    public void setProfile(Profile profile) {

        profileName.setText(profile != null ? profile.getName() : "");
        profilePasswordProtected.setChecked(profile != null ? profile.getPasswordProtectedProfile() : false);

        resetDapps();
        if (profile != null) {
            for (Dapp dapp: profile.getDapps()) {
                adapter.add(dapp);
            }
        }
    }

    protected void resetDapps() {

        adapter.clear();
        adapter.add(addDapp);
    }

    public Profile getProfile() {

        Profile profile = accountPrivateKey != null ? new Profile(accountPrivateKey) : new Profile();
        profile.setName(profileName.getText().toString());
        profile.setPasswordProtectedProfile(profilePasswordProtected.isChecked());
        List<Dapp> dapps = adapter.getItems();
        dapps.remove(addDapp);
        profile.setDapps(dapps);
        return profile;
    }

    protected void editDapp(Dapp dapp) {

        dapEditPosition = adapter.getPosition(dapp);
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
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_profile, container, false);
        profileName = (EditText)view.findViewById(R.id.profile_name);
        profilePasswordProtected = (Switch)view.findViewById(R.id.profile_password_protected);

        accountModeButton = (TextView)view.findViewById(R.id.account_mode);
        dappsList = (RecyclerView) view.findViewById(R.id.profile_dapps_list);

        accountModeDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.account_mode_title)
                .customView(R.layout.account_creation_mode, true)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .contentColor(R.color.accent) // notice no 'res' postfix for literal color
                .dividerColorRes(R.color.accent)
                .backgroundColorRes(R.color.primary_dark)
                .positiveColorRes(R.color.accent)
                .negativeColorRes(R.color.accent)
                .widgetColorRes(R.color.accent)
                .autoDismiss(false)
                .callback(new MaterialDialog.ButtonCallback() {

                    @Override
                    public void onPositive(MaterialDialog dialog) {

                        boolean hideDialog = true;
                        accountPrivateKey = null;
                        switch (accountMode) {
                            case R.id.radio_new_account:

                                break;
                            case R.id.radio_import_file:
                                File file = new File(accountImportSource.getText().toString());
                                if (file.exists()) {
                                    StringBuilder text = new StringBuilder();

                                    try {
                                        BufferedReader buffer = new BufferedReader(new FileReader(file));
                                        String line;

                                        while ((line = buffer.readLine()) != null) {
                                            text.append(line);
                                        }
                                        buffer.close();
                                        accountPrivateKey = text.toString();
                                    } catch (IOException e) {

                                    }
                                } else {
                                    Toast.makeText(getActivity(), "File not found", Toast.LENGTH_LONG).show();
                                    hideDialog = false;
                                }
                                break;
                            case R.id.radio_import_string:
                                accountPrivateKey = accountImportSource.getText().toString();
                                break;
                        }
                        if (hideDialog) {
                            dialog.hide();
                        }

                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {

                        dialog.hide();
                    }
                })
                .build();
        accountModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accountModeDialog.show();
            }
        });
        View accountModeView = accountModeDialog.getCustomView();
        accountModeCreate = (RadioButton)accountModeView.findViewById(R.id.radio_new_account);
        accountModeImportFile = (RadioButton)accountModeView.findViewById(R.id.radio_import_file);
        accountModeImportString = (RadioButton)accountModeView.findViewById(R.id.radio_import_string);
        accountImportSource = (EditText)accountModeView.findViewById(R.id.accout_import_source);

        accountModeCreate.setOnClickListener(accountModeListener);
        accountModeImportFile.setOnClickListener(accountModeListener);
        accountModeImportString.setOnClickListener(accountModeListener);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        dappsList.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getActivity());
        dappsList.setLayoutManager(layoutManager);

        adapter = new DappAdapter(dapps);
        resetDapps();
        dappsList.setAdapter(adapter);

        swipeToAction = new SwipeToAction(dappsList, new SwipeToAction.SwipeListener<Dapp>() {
            @Override
            public boolean swipeLeft(final Dapp itemData) {

                adapter.remove(itemData);
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
            .contentColor(R.color.accent) // notice no 'res' postfix for literal color
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
                        adapter.set(dapEditPosition, dapp);
                    } else {
                        adapter.add(dapp);
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

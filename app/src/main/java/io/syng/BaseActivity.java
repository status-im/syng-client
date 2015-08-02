package io.syng;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.support.v7.widget.Toolbar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.syng.entities.PreferenceManager;
import io.syng.entities.Profile;

public class BaseActivity extends AppCompatActivity {

    Toolbar toolbar;
    protected ActionBarDrawerToggle drawerToggle;
    ArrayList<String> menuItems = new ArrayList<>(Arrays.asList("Console", "DApps", "EtherEx", "TrustDavis", "Augur"));
    protected List<Profile> profiles;

    Spinner spinner;
    EditText search;
    ListView drawerList;

    TextView settings;
    TextView profileManager;

    protected ArrayAdapter<String> drawerListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(final int layoutResID) {

        LayoutInflater inflater = getLayoutInflater();
        DrawerLayout fullLayout = (DrawerLayout) inflater.inflate(R.layout.drawer, null);
        LinearLayout actContent= (LinearLayout) fullLayout.findViewById(R.id.content);

        DrawerLayout drawerLayout = (DrawerLayout) fullLayout.findViewById(R.id.drawer_layout);
        drawerList = (ListView) drawerLayout.findViewById(R.id.drawer_list);
        initDrawer();

        toolbar = (Toolbar) inflater.inflate(layoutResID, actContent, true).findViewById(R.id.myToolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(drawerToggle);

        spinner = (Spinner) drawerLayout.findViewById(R.id.nv_email);
        initSpinner();

        search = (EditText) drawerLayout.findViewById(R.id.search);
        initSearch();

        settings = (TextView) drawerLayout.findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(BaseActivity.this, SettingsActivity.class));
            }
        });

        profileManager = (TextView) drawerLayout.findViewById(R.id.profileManager);
        profileManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(BaseActivity.this, ProfileManagerActivity.class));
            }
        });

        super.setContentView(fullLayout);
    }

    private void initDrawer() {

        drawerListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, (ArrayList)menuItems.clone());
        drawerList.setAdapter(drawerListAdapter);
        drawerListAdapter.notifyDataSetChanged();

        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String item = parent.getItemAtPosition(position).toString();

                switch (item) {
                    case "Console":
                        startActivity(new Intent(BaseActivity.this, MainActivity.class));
                        break;
                    case "DApps":
                        break;
                    case "EtherEx":
                        break;
                    case "TrustDavis":
                        startActivity(new Intent(BaseActivity.this, webview.class));
                        break;
                    case "Augur":
                        break;
                }
            }
        });
    }

    public void initSpinner() {

        profiles = ((Syng)getApplication()).preferenceManager.getProfiles();
        ArrayList<String> spinnerItems = new ArrayList<>();
        for (Profile profile: profiles) {
            spinnerItems.add(profile.getName());
        }
        spinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, spinnerItems.toArray(new String[spinnerItems.size()])));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //String item = (String) adapterView.getItemAtPosition(i);
                if (adapterView != null && adapterView.getChildAt(0) != null) {
                    ((TextView) adapterView.getChildAt(0)).setTextColor(Color.parseColor("#ffffff"));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

                ((TextView) adapterView.getChildAt(0)).setTextColor(Color.parseColor("#ffffff"));
            }

        });
    }

    private void initSearch() {

        search.addTextChangedListener(new TextWatcher() {

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

        search.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {

                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
    }

    protected void updateAppList(String filter) {

        drawerListAdapter.clear();
        int length = menuItems.size();
        for (int i = 0; i < length; i++) {
            String item = menuItems.get(i);
            if (item.toLowerCase().contains(filter.toLowerCase())) {
                drawerListAdapter.add(item);
            }
        }
        drawerListAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {

        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }
}

package io.blockchainsociety.syng;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.support.v7.widget.Toolbar;

public class BaseActivity extends AppCompatActivity {

    Toolbar toolbar;
    public String[] layers;
    private ActionBarDrawerToggle drawerToggle;
    String[] nv_items = {"Console", "DAPP", "Item3", "Item4", "Item5", "Item6", "Item7"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(final int layoutResID) {

        DrawerLayout fullLayout = (DrawerLayout) getLayoutInflater()
                .inflate(R.layout.drawer, null);
        LinearLayout actContent= (LinearLayout) fullLayout.findViewById(R.id.content);

        DrawerLayout drawerLayout = (DrawerLayout) fullLayout.findViewById(R.id.drawer_layout);
        ListView drawerList = (ListView) fullLayout.findViewById(R.id.drawer_list);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, nv_items);
        drawerList.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0) {

                    startActivity(new Intent(BaseActivity.this, MainActivity.class));

                } else if (position == 1) {

                    startActivity(new Intent(BaseActivity.this, webview.class));

                } else if (position == 2) {

                }
            }
        });

        toolbar = (Toolbar) getLayoutInflater().inflate(layoutResID, actContent, true).findViewById(R.id.myToolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(drawerToggle);
        /*
        spinner = (Spinner)findViewById(R.id.nv_email);
        //spinner.setAdapter(new ArrayAdapter<String>(this, R.layout.text, spinner_items));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String item = (String) adapterView.getItemAtPosition(i);
                ((TextView) adapterView.getChildAt(0)).setTextColor(Color.parseColor("#ffffff"));
            }

            @Override

            public void onNothingSelected(AdapterView<?> adapterView) {

                ((TextView) adapterView.getChildAt(0)).setTextColor(Color.parseColor("#ffffff"));
            }

        });
        */


        super.setContentView(fullLayout);
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

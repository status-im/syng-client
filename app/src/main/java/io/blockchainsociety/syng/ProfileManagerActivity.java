package io.blockchainsociety.syng;

import android.app.FragmentManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import io.blockchainsociety.syng.entities.Profile;


public class ProfileManagerActivity extends BaseActivity implements OnFragmentInteractionListener {

    private FragmentManager fragmentManager;

    private AddProfileFragment addProfileFragment;
    private ProfileManagerFragment profileManagerFragment;

    private TextView saveProfileLink;
    private TextView addProfileLink;

    private View profileManagerContainer;
    private View addProfileContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_manager);

        saveProfileLink = (TextView)findViewById(R.id.save_profile_link);
        saveProfileLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Profile profile = addProfileFragment.getProfile();
                profileManagerFragment.addProfile(profile);
                hideAddProfile();
            }
        });
        addProfileLink = (TextView)findViewById(R.id.add_profile_link);
        addProfileLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                profileManagerFragment.resetProfilePosition();
                showAddProfile(null);
            }
        });

        if (savedInstanceState == null) {
            addProfileFragment = new AddProfileFragment();
            profileManagerFragment = new ProfileManagerFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.profileManagerFragmentContainer, profileManagerFragment)
                    .add(R.id.addProfileFragmentContainer, addProfileFragment)
                    .commit();
        }

        addProfileContainer = findViewById(R.id.addProfileFragmentContainer);
        profileManagerContainer = findViewById(R.id.profileManagerFragmentContainer);



        hideAddProfile();
    }

    public void showAddProfile(Profile profile) {

        profileManagerContainer.setVisibility(View.INVISIBLE);
        addProfileContainer.setVisibility(View.VISIBLE);
        addProfileLink.setVisibility(View.INVISIBLE);
        saveProfileLink.setVisibility(View.VISIBLE);

        addProfileFragment.setProfile(profile);

        getSupportActionBar().setTitle(R.string.add_profile);
    }

    public void hideAddProfile() {

        addProfileContainer.setVisibility(View.INVISIBLE);
        profileManagerContainer.setVisibility(View.VISIBLE);
        addProfileLink.setVisibility(View.VISIBLE);
        saveProfileLink.setVisibility(View.INVISIBLE);

        getSupportActionBar().setTitle(R.string.profile_manager_title);
    }

    public void addProfile(Profile profile) {

        profileManagerFragment.addProfile(profile);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile_manager, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}

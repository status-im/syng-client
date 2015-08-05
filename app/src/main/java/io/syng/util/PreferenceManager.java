package io.syng.util;


import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

import io.syng.entity.ObjectSerializer;
import io.syng.entity.Profile;

public class PreferenceManager {

    private final static String SHARED_PREFERENCES_FILE = "test";

    private SharedPreferences mPreferences;

    public PreferenceManager(Context context) {
        mPreferences = context.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    public void saveProfiles(ArrayList<Profile> profiles) {
        SharedPreferences.Editor editor = mPreferences.edit();
        try {
            editor.putString("profiles", ObjectSerializer.serialize(profiles));
        } catch (Exception e) {
            e.printStackTrace();
        }
        editor.apply();
    }

    public ArrayList<Profile> getProfiles() {
        ArrayList<Profile> profiles = new ArrayList<>();
        try {
            profiles = (ArrayList<Profile>) ObjectSerializer.deserialize(mPreferences.getString("profiles", ObjectSerializer.serialize(profiles)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return profiles;
    }

    public void close() {
        mPreferences = null;
    }

}

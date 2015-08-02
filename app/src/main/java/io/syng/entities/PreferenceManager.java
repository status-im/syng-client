package io.syng.entities;


import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PreferenceManager {

    private SharedPreferences preferences;

    private static String sharedPreferencesFile = "test";

    public PreferenceManager(Context context) {

        preferences = context.getSharedPreferences(sharedPreferencesFile, Context.MODE_PRIVATE);
    }

    public void saveProfiles(ArrayList<Profile> profiles) {

        SharedPreferences.Editor editor = preferences.edit();
        try {
            editor.putString("profiles", ObjectSerializer.serialize(profiles));
        } catch (Exception e) {
            e.printStackTrace();
        }
        editor.commit();
    }

    public ArrayList<Profile> getProfiles() {

        ArrayList<Profile> profiles = new ArrayList<>();
        try {
            profiles = (ArrayList<Profile>) ObjectSerializer.deserialize(preferences.getString("profiles", ObjectSerializer.serialize(profiles)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return profiles;
    }

    public void close() {

        preferences = null;
    }
}

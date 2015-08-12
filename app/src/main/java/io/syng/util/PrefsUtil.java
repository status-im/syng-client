package io.syng.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.ArrayList;

import io.syng.entity.ObjectSerializer;
import io.syng.entity.Profile;

public final class PrefsUtil {

    private final static String SHARED_PREFERENCES_FILE = "test";

    private static final String PROFILES_KEY = "pref_profile_key";
    private static final String FIRST_LAUNCH_KEY = "first_launch_key";

    private static PrefsUtil sInstance;

    private SharedPreferences mPreferences;

    private PrefsUtil(Context context) {
        mPreferences = context.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    public static void initialize(Context context) {
        if (sInstance != null) {
            throw new IllegalStateException("PrefsUtil have already been initialized");
        }
        sInstance = new PrefsUtil(context);
    }

    private static PrefsUtil getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("PrefsUtil should be initialized first");
        }
        return sInstance;
    }

    private static Editor getEditor() {
        return getPrefs().edit();
    }

    private static SharedPreferences getPrefs() {
        return getInstance().mPreferences;
    }

    public static void saveProfiles(ArrayList<Profile> profiles) {
        try {
            SharedPreferences.Editor editor = getEditor();
            editor.putString(PROFILES_KEY, ObjectSerializer.serialize(profiles));
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Profile> getProfiles() {
        ArrayList<Profile> profiles = new ArrayList<>();
        try {
            profiles = (ArrayList<Profile>) ObjectSerializer.deserialize(getPrefs().getString(PROFILES_KEY, ObjectSerializer.serialize(profiles)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return profiles;
    }

    public static void updateProfile(Profile profile) {

        ArrayList<Profile> profiles = getProfiles();
        for (Profile item: profiles) {
            if (item.getId().equals(profile.getId())) {
                int index = profiles.indexOf(item);
                profiles.set(index, profile);
                saveProfiles(profiles);
                break;
            }
        }
    }

    public static boolean saveProfile(Profile profile) {

        ArrayList<Profile> profiles = PrefsUtil.getProfiles();
        for (Profile item: profiles) {
            if (item.getName().equals(profile.getName())) {
                return false;
            }
        }
        profiles.add(profile);
        saveProfiles(profiles);
        return true;
    }


    public static void setFirstLaunch(boolean isFirstLaunch) {
        getEditor().putBoolean(FIRST_LAUNCH_KEY, isFirstLaunch).apply();
    }

    public static boolean isFirstLaunch() {
        return getPrefs().getBoolean(FIRST_LAUNCH_KEY, true);
    }

}

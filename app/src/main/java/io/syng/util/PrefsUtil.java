package io.syng.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import java.util.ArrayList;

import io.syng.R;
import io.syng.entity.ObjectSerializer;
import io.syng.entity.Profile;

public final class PrefsUtil {

    private static final String PROFILES_KEY = "pref_profile_key";
    private static final String FIRST_LAUNCH_KEY = "first_launch_key";
    private static final String CURRENT_PROFILE_KEY = "current_profile";

    private static PrefsUtil sInstance;
    private final Context mContext;

    private SharedPreferences mPreferences;

    private PrefsUtil(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mContext = context;
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

    private static String getString(@StringRes int resourceId) {
        return getInstance().mContext.getString(resourceId);
    }

    private static void saveProfiles(ArrayList<Profile> profiles) {
        try {
            getEditor().putString(PROFILES_KEY, ObjectSerializer.serialize(profiles)).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<Profile> getProfiles() {
        ArrayList<Profile> profiles = new ArrayList<>();
        try {
            profiles = (ArrayList<Profile>) ObjectSerializer.deserialize(
                    getPrefs().getString(PROFILES_KEY, ObjectSerializer.serialize(profiles)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return profiles;
    }

    public static void updateProfile(Profile profile) {

        ArrayList<Profile> profiles = getProfiles();
        for (Profile item : profiles) {
            if (item.getId().equals(profile.getId())) {
                int index = profiles.indexOf(item);
                profiles.set(index, profile);
                saveProfiles(profiles);
                break;
            }
        }
    }

    public static boolean addProfile(Profile profile) {
        ArrayList<Profile> profiles = PrefsUtil.getProfiles();
        profiles.add(profile);
        saveProfiles(profiles);
        return true;
    }

    public static void setCurrentProfileId(String profileId) {
        getEditor().putString(CURRENT_PROFILE_KEY, profileId).commit();
    }

    public static String getCurrentProfileId() {
        return getPrefs().getString(CURRENT_PROFILE_KEY, "");
    }

    public static void setFirstLaunch(boolean isFirstLaunch) {
        getEditor().putBoolean(FIRST_LAUNCH_KEY, isFirstLaunch).apply();
    }

    public static boolean isFirstLaunch() {
        return getPrefs().getBoolean(FIRST_LAUNCH_KEY, true);
    }


    public static void setBackgroundResourceId(String profileId, @DrawableRes int resourceId) {
        getEditor().putInt(profileId, resourceId).commit();
    }

    public static int getBackgroundResourceId(String profileId) {
        return getPrefs().getInt(profileId, R.drawable.bg0_resized);
    }

    public static String getJsonRPCServerAddress() {
        return getPrefs().getString(getString(R.string.pref_json_rpc_server_key), getString(R.string.pref_json_rpc_server_default));
    }

}

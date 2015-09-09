/*
 * Copyright (c) 2015 Jarrad Hope
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.syng.util;

import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.syng.app.SyngApplication;
import io.syng.entity.Dapp;
import io.syng.entity.Profile;

public final class ProfileManager {

    public interface ProfilesChangeListener {
        void onProfilesChange();
    }

    private static ProfileManager sInstance;

    private ProfilesChangeListener mProfilesChangeListener;

    public static void initialize() {
        if (sInstance != null) {
            throw new IllegalStateException("ProfileManager have already been initialized");
        }
        sInstance = new ProfileManager();
    }

    private static ProfileManager getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("ProfileManager should be initialized first");
        }
        return sInstance;
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<Profile> getProfiles() {
        return PrefsUtil.getProfiles();
    }

    public static void addProfile(Profile profile) {
        ArrayList<Profile> profiles = PrefsUtil.getProfiles();
        profiles.add(profile);
        PrefsUtil.saveProfiles(profiles);
        notifyListener();
    }

    public static void updateProfile(Profile profile) {
        ArrayList<Profile> profiles = getProfiles();
        for (Profile item : profiles) {
            if (item.getId().equals(profile.getId())) {
                int index = profiles.indexOf(item);
                profiles.set(index, profile);
                PrefsUtil.saveProfiles(profiles);
                break;
            }
        }
        notifyListener();
    }

    public static Profile getCurrentProfile() {
        // Add default cow account if not present
//        if (ProfileManager.getProfiles().isEmpty()) {
//            Profile profile = new Profile();
//            profile.setName("Cow");
//            // Add default cow and monkey addresses
//            List<String> addresses = new ArrayList<>();
//            byte[] cowAddr = HashUtil.sha3("cow".getBytes());
//            addresses.add(Hex.toHexString(cowAddr));
//            String secret = CONFIG.coinbaseSecret();
//            byte[] cbAddr = HashUtil.sha3(secret.getBytes());
//            addresses.add(Hex.toHexString(cbAddr));
//            profile.setPrivateKeys(addresses);
//            profile.setPassword("qw");
//            ProfileManager.addProfile(profile);
//            ProfileManager.setCurrentProfile(profile);
//            return profile;
//        }

        List<Profile> profiles = ProfileManager.getProfiles();
        String currentProfileId = PrefsUtil.getCurrentProfileId();

        for (int i = 0; i < profiles.size(); i++) {
            if (currentProfileId.equals(profiles.get(i).getId())) {
                return profiles.get(i);
            }
        }

        return new Profile();
    }

    public static void setCurrentProfile(Profile profile) {
        List<String> privateKeys = profile.getPrivateKeys();
        SyngApplication.sEthereumConnector.init(privateKeys);
        PrefsUtil.setCurrentProfileId(profile.getId());
        notifyListener();
    }

    public static void addDAppToProfile(Profile profile, Dapp dapp) {
        profile.addDapp(dapp);
        ProfileManager.updateProfile(profile);
        notifyListener();
    }

    public static void removeDAppInProfile(Profile profile, Dapp dapp) {
        profile.removeDapp(dapp);
        ProfileManager.updateProfile(profile);
        notifyListener();
    }

    public static void updateDAppInProfile(Profile profile, Dapp dapp) {
        profile.updateDapp(dapp);
        ProfileManager.updateProfile(profile);
        notifyListener();
    }

    public static void reorderDAppsInProfile(Profile profile, int fromPosition, int toPosition) {
        List<Dapp> dapps = profile.getDapps();
        Collections.swap(dapps, fromPosition, toPosition);
        profile.setDapps(dapps);

        ArrayList<Profile> profiles = getProfiles();
        for (Profile item : profiles) {
            if (item.getId().equals(profile.getId())) {
                int index = profiles.indexOf(item);
                profiles.set(index, profile);
                PrefsUtil.saveProfiles(profiles);
                break;
            }
        }
    }

    public static void reorderProfiles(int fromPosition, int toPosition) {
        List<Profile> profiles = ProfileManager.getProfiles();
        Collections.swap(profiles, fromPosition, toPosition);
        PrefsUtil.saveProfiles(new ArrayList<>(profiles));
    }

    @Nullable
    public static Profile getProfileById(String profileId) {
        List<Profile> profiles = ProfileManager.getProfiles();
        for (Profile profile : profiles) {
            if (profile.getId().equals(profileId)) {
                return profile;
            }
        }
        return null;
    }

    public static void setCurrentProfileBackgroundResourceId(@DrawableRes int resourceId) {
        PrefsUtil.setBackgroundResourceId(ProfileManager.getCurrentProfile().getId(), resourceId);
        notifyListener();
    }

    public static int getCurrentProfileBackgroundResourceId() {
        return PrefsUtil.getBackgroundResourceId(ProfileManager.getCurrentProfile().getId());
    }

    public static void setProfilesChangeListener(ProfilesChangeListener listener) {
        getInstance().mProfilesChangeListener = listener;
    }

    public static void removeProfilesChangeListener(ProfilesChangeListener listener) {
        getInstance().mProfilesChangeListener = null;
    }

    private static void notifyListener() {
        if (getInstance().mProfilesChangeListener != null) {
            getInstance().mProfilesChangeListener.onProfilesChange();
        }
    }

}

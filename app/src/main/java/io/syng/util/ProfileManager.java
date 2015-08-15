package io.syng.util;

import org.ethereum.crypto.HashUtil;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;

import io.syng.app.SyngApplication;
import io.syng.entity.Dapp;
import io.syng.entity.Profile;

import static org.ethereum.config.SystemProperties.CONFIG;

public final class ProfileManager {

    private static ProfileManager sInstance;

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

    public static boolean addProfile(Profile profile) {
        return PrefsUtil.addProfile(profile);
    }

    public static void updateProfile(Profile profile) {
        PrefsUtil.updateProfile(profile);
    }

    public static Profile getCurrentProfile() {
        // Add default cow account if not present
        if (ProfileManager.getProfiles().isEmpty()) {
            Profile profile = new Profile();
            profile.setName("Cow");
            // Add default cow and monkey addresses
            List<String> addresses = new ArrayList<>();
            byte[] cowAddr = HashUtil.sha3("cow".getBytes());
            addresses.add(Hex.toHexString(cowAddr));
            String secret = CONFIG.coinbaseSecret();
            byte[] cbAddr = HashUtil.sha3(secret.getBytes());
            addresses.add(Hex.toHexString(cbAddr));
            profile.setPrivateKeys(addresses);
            profile.setPassword("qw");
            ProfileManager.addProfile(profile);
            ProfileManager.setCurrentProfile(profile);
            return profile;
        }

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
    }

    public static void addDAppToProfile(Profile profile, Dapp dapp) {
        profile.addDapp(dapp);
        ProfileManager.updateProfile(profile);
    }


    public static void updateDAppInProfile(Profile profile, Dapp dapp) {
        profile.updateDapp(dapp);
        ProfileManager.updateProfile(profile);
    }

}

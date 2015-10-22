/*
 * Copyright (c) 2015 Jarrad Hope
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.syng.entity;

import android.util.Log;

import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.ethereum.wallet.EtherSaleWallet;
import org.ethereum.wallet.EtherSaleWalletDecoder;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.syng.util.Encryption;

public class Profile implements Serializable {

    private static final String TAG = "Profile";

    protected String name;

    protected String id = createPrivateKey();

    protected List<String> privateKeys = new ArrayList<>();

    protected List<String> publicKeys = new ArrayList<>();

    /* "password protect profile" (encrypt the private keys) */
    protected boolean passwordProtectedProfile;

    protected List<Dapp> dapps = new ArrayList<>();

    private static final long serialVersionUID = 1L;

    protected String passwordHash;

    protected transient boolean isEncrypted;


    public Profile() {
        addPrivateKey(createPrivateKey(), null);
        addDefaultApps();
    }

    public Profile(String privateKey) {
        addPrivateKey(privateKey, null);
        addDefaultApps();
    }

    public Profile(List<String> privateKeys) {
        for (String privateKey: privateKeys) {
            addPrivateKey(privateKey, null);
        }
        addDefaultApps();
    }

    protected void addDefaultApps() {

        // Add console dapp
        Dapp console = new Dapp("Console");
        dapps.add(console);

        // Add wallet dapp
        Dapp wallet = new Dapp("Wallet");
        wallet.setUrl("dapp://syng.io/dapps/wallet");
        dapps.add(wallet);

        // Add contacts dapp
        Dapp contacts = new Dapp("Contacts");
        contacts.setUrl("dapp://syng.io/dapps/contacts");
        dapps.add(contacts);
    }

    protected String createPrivateKey() {
        byte[] privateKey = HashUtil.sha3(HashUtil.randomPeerId());
        return Hex.toHexString(privateKey);
    }

    public List<String> getPrivateKeys(String password) {
        List<String> keys = new ArrayList<>();

        for (String privateKey: privateKeys) {
            String key = passwordProtectedProfile ? decryptPrivateKey(privateKey, password) : privateKey;
            keys.add(key);
        }
        return keys;
    }

    public List<String> getAddresses() {
        return publicKeys;
    }

    public void addPrivateKeys(List<String> privateKeys, String password) {
        for (String privateKey: privateKeys) {
            addPrivateKey(privateKey, password);
        }
    }

    protected String getPublicKey(String privateKey) {
        ECKey ecKey = ECKey.fromPrivate(Hex.decode(privateKey));
        return Hex.toHexString(ecKey.getAddress());
    }

    public boolean addPrivateKey(String privateKey, String password) {

        if (password != null && passwordProtectedProfile) {
            if (!checkPassword(password)) {
                return false;
            }
            this.privateKeys.add(encryptPrivateKey(privateKey, password));
        } else {
            this.privateKeys.add(privateKey);
        }
        this.publicKeys.add(getPublicKey(privateKey));
        return true;
    }

    private String hash(String text) {
        return Hex.toHexString(HashUtil.sha3(text.getBytes()));
    }

    public void removePrivateKey(String privateKey) {

        this.privateKeys.remove(privateKey);
        this.publicKeys.remove(getPublicKey(privateKey));
    }

    public List<Dapp> getDapps() {
        return dapps;
    }

    public void setDapps(List<Dapp> dapps) {
        this.dapps = dapps;
    }

    public void addDapp(Dapp dapp) {
        this.dapps.add(dapp);
    }

    public void updateDapp(Dapp dapp) {
        for (Dapp item : dapps) {
            if (item.getId().equals(dapp.getId())) {
                int index = dapps.indexOf(item);
                dapps.set(index, dapp);
            }
        }
    }

    public void removeDapp(Dapp dapp) {
        this.dapps.remove(dapp);
    }

    public boolean getPasswordProtectedProfile() {
        return passwordProtectedProfile;
    }

    public void setPasswordProtectedProfile(boolean passwordProtectedProfile) {
        this.passwordProtectedProfile = passwordProtectedProfile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setPassword(String password) {
        if (!passwordProtectedProfile) {
            this.passwordHash = hash(password);
            this.encrypt(password);
        }
    }

    public boolean checkPassword(String password) {
        return passwordHash.equals(hash(password));
    }

    public void encrypt(String password) {
        if (!passwordProtectedProfile) {
            List<String> encrypted = new ArrayList<>();
            for (String privateKey : this.privateKeys) {
                encrypted.add(encryptPrivateKey(privateKey, password));
            }
            this.privateKeys.clear();
            this.privateKeys = encrypted;
            passwordProtectedProfile = true;
        }
    }

    public boolean decrypt(String password) {
        if (passwordProtectedProfile) {
            if (!checkPassword(password)) {
                return false;
            }
            List<String> decrypted = new ArrayList<>();
            for (String privateKey : this.privateKeys) {
                decrypted.add(decryptPrivateKey(privateKey, password));
            }
            this.privateKeys = decrypted;
            passwordProtectedProfile = false;
        }
        return true;
    }

    protected String encryptPrivateKey(String privateKey, String password) {
        String encryptedKey = Encryption.encrypt(privateKey, password);
        if (encryptedKey == null) {
            Log.w(TAG, "Could not encrypt private key");
        }
        return encryptedKey;
    }

    protected String decryptPrivateKey(String privateKey, String password) {
        String decryptedKey = Encryption.decrypt(privateKey, password);
        if (decryptedKey == null) {
            Log.w(TAG, "Could not decrypt private key");
        }
        return decryptedKey;
    }

    public boolean importWallet(String jsonWallet, String importPassword, String currentPassword) {
        try {
            JSONObject json = new JSONObject(jsonWallet);
            byte[] privateKey = null;
            EtherSaleWallet wallet = new EtherSaleWallet();
            if (json.has("encseed")) {
                wallet.setEncseed(json.getString("encseed"));
                wallet.setEthaddr(json.getString("ethaddr"));
                wallet.setEmail(json.getString("email"));
                wallet.setBtcaddr(json.getString("btcaddr"));
                EtherSaleWalletDecoder decoder = new EtherSaleWalletDecoder(wallet);
                privateKey = decoder.getPrivateKey(importPassword);
            } else if (json.has("Crypto")) {
                wallet.setEncseed(json.getJSONObject("Crypto").getJSONObject("cipherparams").getString("iv") + json.getJSONObject("Crypto").getString("ciphertext"));
                wallet.setEthaddr(json.getString("address"));
                EtherSaleWalletDecoder decoder = new EtherSaleWalletDecoder(wallet);
                privateKey = decoder.getPrivateKey(importPassword);
            }
            if (privateKey == null) {
                Log.w(TAG, "Invalid json wallet file.");
                return false;
            }
            ECKey key = ECKey.fromPrivate(privateKey);
            String address = Hex.toHexString(key.getAddress());
            if (address.equals(wallet.getEthaddr())) {
                String keyToAdd = Hex.toHexString(privateKey);
                if (passwordProtectedProfile) {
                    addPrivateKey(keyToAdd, currentPassword);
                } else {
                    addPrivateKey(keyToAdd, null);
                }
                this.publicKeys.add(getPublicKey(keyToAdd));
            } else {
                Log.w(TAG, "Invalid wallet password.");
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error importing wallet: " + e.getMessage());
            return false;
        }

        return true;
    }

    public boolean importPrivateKey(String privateKey, String importedKeyPassword, String walletPassword) {
        String decryptedKey = importedKeyPassword == null || importedKeyPassword == "" ? privateKey : decryptPrivateKey(privateKey, importedKeyPassword);
        if (passwordProtectedProfile) {
            if (checkPassword(walletPassword)) {
                addPrivateKey(decryptedKey, walletPassword);
            } else {
                return false;
            }
        } else {
            addPrivateKey(decryptedKey, null);
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Profile)) return false;
        Profile object = (Profile) o;
        return object.getId().equalsIgnoreCase(id);
    }

}

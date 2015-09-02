/*
 * Copyright (c) 2015 Jarrad Hope
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.syng.entity;

import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.ethereum.wallet.EtherSaleWallet;
import org.ethereum.wallet.EtherSaleWalletDecoder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Profile implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger("Profile");

    protected String name;

    protected String id = createPrivateKey();

    protected List<String> privateKeys = new ArrayList<>();

    /* "password protect profile" (encrypt the private keys) */
    protected boolean passwordProtectedProfile;

    protected List<Dapp> dapps = new ArrayList<>();

    private static final long serialVersionUID = 1L;

    protected String passwordHash;

    protected transient boolean isEncrypted;

    public Profile() {
        this.privateKeys.add(createPrivateKey());
        addDefaultApps();
    }

    public Profile(String privateKey) {
        this.privateKeys.add(privateKey);
        addDefaultApps();
    }

    public Profile(List<String> privateKeys) {
        this.privateKeys = privateKeys;
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

    public List<String> getPrivateKeys() {
        return privateKeys;
    }

    public List<String> getAddresses() {

        List<String> addresses = new ArrayList<>();
        for (String privateKey: privateKeys) {
            ECKey key = ECKey.fromPrivate(Hex.decode(privateKey));
            addresses.add(Hex.toHexString(key.getAddress()));
        }
        return addresses;
    }

    public void setPrivateKeys(List<String> privateKeys) {
        this.privateKeys = privateKeys;
    }

    public void addPrivateKey(String privateKey) {
        this.privateKeys.add(privateKey);
    }

    public void removePrivateKey(String privateKey) {
        this.privateKeys.remove(privateKey);
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
        this.passwordHash = Hex.toHexString(HashUtil.sha3(password.getBytes()));
    }

    public boolean checkPassword(String password) {
        return passwordHash.equals(Hex.toHexString(HashUtil.sha3(password.getBytes())));
    }

    public void encrypt(String password) {
        if (!passwordProtectedProfile) {
            setPassword(password);
            List<String> encrypted = new ArrayList<>();
            for (String privateKey : this.privateKeys) {
                encrypted.add(encryptPrivateKey(privateKey, password));
            }
            this.privateKeys = encrypted;
            passwordProtectedProfile = true;
        }
    }

    public boolean decrypt(String password) {
        if (passwordProtectedProfile) {
            if (!passwordHash.equals(Hex.toHexString(HashUtil.sha3(password.getBytes())))) {
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
        // TODO: Encrypt private key
        return privateKey;
    }

    protected String decryptPrivateKey(String privateKey, String password) {
        // TODO: Decrypt private key
        return privateKey;
    }

    public boolean importWallet(String jsonWallet, String password) {
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
                privateKey = decoder.getPrivateKey(password);
            } else if (json.has("Crypto")) {
                wallet.setEncseed(json.getJSONObject("Crypto").getJSONObject("cipherparams").getString("iv") + json.getJSONObject("Crypto").getString("ciphertext"));
                wallet.setEthaddr(json.getString("address"));
                EtherSaleWalletDecoder decoder = new EtherSaleWalletDecoder(wallet);
                privateKey = decoder.getPrivateKey(password);
            }
            if (privateKey == null) {
                logger.warn("Invalid json wallet file.");
                return false;
            }
            ECKey key = ECKey.fromPrivate(privateKey);
            String address = Hex.toHexString(key.getAddress());
            if (address.equals(wallet.getEthaddr())) {
                privateKeys.add(Hex.toHexString(privateKey));
            } else {
                logger.warn("Invalid wallet password.");
                return false;
            }
        } catch (Exception e) {
            logger.error("Error importing wallet.", e);
            return false;
        }

        return true;
    }

    public void importPrivateKey(String privateKey, String password) {
        privateKeys.add(decryptPrivateKey(privateKey, password));
    }

}

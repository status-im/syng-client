package io.syng.entity;

import org.ethereum.crypto.HashUtil;
import org.spongycastle.util.encoders.Hex;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Profile implements Serializable {

    protected String name;

    protected List<String> privateKeys = new ArrayList<>();

    /* "password protect profile" (encrypt the private keys) */
    protected boolean passwordProtectedProfile = false;

    protected List<Dapp> dapps;

    private static final long serialVersionUID = 1L;

    protected String passwordHash;

    protected transient boolean isEncrypted = false;

    public Profile() {

        this.privateKeys.add(createPrivateKey());
    }

    public Profile(String privateKey) {

        this.privateKeys.add(privateKey);
    }

    public Profile(List<String> privateKeys) {

        this.privateKeys = privateKeys;
    }

    protected String createPrivateKey() {

        byte[] privateKey = HashUtil.sha3(HashUtil.randomPeerId());
        return Hex.toHexString(privateKey);
    }

    public List<String> getPrivateKeys() {

        return privateKeys;
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

    protected void setPassword(String password) {

        this.passwordHash = Hex.toHexString(HashUtil.sha3(password.getBytes()));
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
            if (passwordHash != Hex.toHexString(HashUtil.sha3(password.getBytes()))) {
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
}

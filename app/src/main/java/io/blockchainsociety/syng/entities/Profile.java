package io.blockchainsociety.syng.entities;

import java.io.Serializable;
import java.util.List;

public class Profile implements Serializable {

    protected String name;

    protected String privateKey;

    /* "password protect profile" (encrypt the private keys) */
    protected boolean passwordProtectedProfile = false;

    protected List<Dapp> dapps;

    private static final long serialVersionUID = 1L;

    public Profile() {

        this.privateKey = "new key";
    }

    public String getPrivateKey() {

        if (passwordProtectedProfile) {
            return decryptPrivateKey(privateKey, null);
        } else {
            return privateKey;
        }
    }

    public void setPrivateKey(String privateKey) {

        if (passwordProtectedProfile) {
            this.privateKey = encryptPrivateKey(privateKey, null);
        } else {
            this.privateKey = privateKey;
        }
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
        if (passwordProtectedProfile) {
            this.privateKey = encryptPrivateKey(privateKey, null);
        }
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
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

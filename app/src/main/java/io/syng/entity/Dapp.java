package io.syng.entity;

import org.ethereum.crypto.HashUtil;
import org.spongycastle.util.encoders.Hex;

import java.io.Serializable;

public class Dapp implements Serializable {

    protected String name = "";
    protected String version = "";
    protected String url = "";
    protected String id = "";

    private static final long serialVersionUID = 1L;

    public Dapp(String id, String name) {

        this.id = id;
        this.name = name;
    }

    public Dapp(String name) {

        this.name = name;
        this.id = generateID();
    }

    public Dapp() {

        this.id = generateID();
    }

    protected String generateID() {

        byte[] privateKey = HashUtil.sha3(HashUtil.randomPeerId());
        return Hex.toHexString(privateKey);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}

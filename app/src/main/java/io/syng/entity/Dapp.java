package io.syng.entity;

public class Dapp {

    protected String name = "";
    protected String version = "";
    protected String url = "";
    protected String id;

    public Dapp(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Dapp(String name) {
        this.name = name;
    }

    public Dapp() {
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

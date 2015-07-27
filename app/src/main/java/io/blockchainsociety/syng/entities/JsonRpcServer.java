package io.blockchainsociety.syng.entities;

import java.io.Serializable;

public class JsonRpcServer implements Serializable {

    protected String host;

    protected int port;

    private static final long serialVersionUID = 1L;

    public JsonRpcServer() {

    }

    public String getHost() {

        return host;
    }

    public void setHost(String host) {

        this.host = host;
    }

    public int getPort() {

        return port;
    }

    public void setPort(int port) {

        this.port = port;
    }
}

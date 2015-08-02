package io.syng.entities;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Settings implements Serializable {

    protected int runningMode = RunningMode.JSON_RPC_CLIENT;

    /* "sync in background" option */
    protected boolean syncInBackground = false;

    /* "sync only when connected to wifi" */
    protected boolean syncOnlyWhenWifi = true;

    /* "setting json-rpc servers (address and port)" */
    protected List<JsonRpcServer> jsonRpcServers = new ArrayList<>();

    private static final long serialVersionUID = 1L;

    public Settings() {

    }

    public int getRunningMode() {

        return runningMode;
    }

    public void setRunningMode(int runningMode) {

        this.runningMode = runningMode;
    }

    public boolean getSyncInBackground() {

        return syncInBackground;
    }

    public void setSyncInBackground(boolean syncInBackground) {

        this.syncInBackground = syncInBackground;
    }

    public boolean getSyncOnlyWhenWifi() {

        return syncOnlyWhenWifi;
    }

    public void setSyncOnlyWhenWifi(boolean syncOnlyWhenWifi) {

        this.syncOnlyWhenWifi = syncOnlyWhenWifi;
    }

    public List<JsonRpcServer> getJsonRpcServers() {

        return jsonRpcServers;
    }

    public void setJsonRpcServers(List<JsonRpcServer> jsonRpcServers) {

        this.jsonRpcServers = jsonRpcServers;
    }

    public void addJsonRpcServer(String host, int port) {

        JsonRpcServer server = new JsonRpcServer();
        server.setHost(host);
        server.setPort(port);
        jsonRpcServers.add(server);
    }

    public void addJsonRpcServer(JsonRpcServer jsonRpcServer) {

        this.jsonRpcServers.add(jsonRpcServer);
    }

    public void removeJsonRpcServer(JsonRpcServer jsonRpcServer) {

        this.jsonRpcServers.remove(jsonRpcServer);
    }
}

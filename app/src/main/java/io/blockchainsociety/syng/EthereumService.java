package io.blockchainsociety.syng;

import org.ethereum.android.EthereumAidlService;
import org.ethereum.android.interop.IListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EthereumService extends EthereumAidlService {


    private static final Logger logger = LoggerFactory.getLogger("EthereumService");

    public EthereumService() {

    }

    @Override
    protected void broadcastMessage(String message) {

        updateLog(message);
        for (IListener listener: clientListeners) {
            try {
                listener.trace(message);
            } catch (Exception e) {
                // Remove listener
                clientListeners.remove(listener);
            }
        }
    }

    private void updateLog(String message) {

        EthereumService.log += message;
        int logLength = EthereumService.log.length();
        if (logLength > 100000) {
            EthereumService.log = EthereumService.log.substring(50000);
        }
    }

}

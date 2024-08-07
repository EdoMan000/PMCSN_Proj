package org.pmcsn.configuration;

import org.pmcsn.centers.Dummy_MultiServer;
import org.pmcsn.centers.Dummy_SingleServer;

public class CenterFactory {
    private final ConfigurationManager configurationManager = new ConfigurationManager();

    public CenterFactory() {}

    public Dummy_SingleServer createDummySingleServer(boolean approximateServiceAsExponential) {
        return new Dummy_SingleServer(
                configurationManager.getString("dummy_SingleServer", "centerName"),
                configurationManager.getDouble("dummy_SingleServer", "meanServiceTime"),
                configurationManager.getInt("dummy_SingleServer", "streamIndex"),
                approximateServiceAsExponential);
    }

    public Dummy_MultiServer createDummyMultiServer(boolean approximateServiceAsExponential) {
        return new Dummy_MultiServer(
                configurationManager.getString("dummy_MultiServer", "centerName"),
                configurationManager.getDouble("dummy_MultiServer", "meanServiceTime"),
                configurationManager.getInt("dummy_MultiServer", "serversNumber"),
                configurationManager.getInt("dummy_MultiServer", "streamIndex"),
                approximateServiceAsExponential);
    }
}

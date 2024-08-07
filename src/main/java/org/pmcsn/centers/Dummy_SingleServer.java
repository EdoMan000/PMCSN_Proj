package org.pmcsn.centers;

import org.pmcsn.configuration.ConfigurationManager;
import org.pmcsn.libraries.Rngs;
import org.pmcsn.model.EventQueue;
import org.pmcsn.model.EventType;
import org.pmcsn.model.MsqEvent;
import org.pmcsn.model.MsqTime;

import static org.pmcsn.utils.Distributions.exponential;
public class Dummy_SingleServer extends SingleServer {

    private double sarrival;
    private boolean endOfArrivals;

    public Dummy_SingleServer(String centerName, double meanServiceTime, int streamIndex, boolean approximateServiceAsExponential) {
        super(centerName, meanServiceTime, streamIndex, approximateServiceAsExponential);
    }

    public void start(Rngs rngs, double sarrival){
        this.rngs = rngs;
        this.sarrival = sarrival;
        this.endOfArrivals = false;
        reset(rngs);
    }


    @Override
    public void spawnNextCenterEvent(MsqTime time, EventQueue queue) {

        EventType type = EventType.ARRIVAL;
        MsqEvent event = new MsqEvent(type, getArrival());
        queue.add(event);

    }



    @Override
    public void spawnCompletionEvent(MsqTime time, EventQueue queue) {
        double service = getService(streamindex);
        MsqEvent event = new MsqEvent(EventType.DONE, time.current + service, service);
        queue.add(event);
    }

    protected double getService(int streamIndex) {
        rngs.selectStream(streamIndex);
        return exponential(meanServiceTime, rngs);
    }

    public double getArrival() {
        ConfigurationManager config = new ConfigurationManager();
        rngs.selectStream(streamindex + 1);
        sarrival += exponential(config.getDouble("general", "interArrivalTime"), rngs);
        return (sarrival);
    }
}
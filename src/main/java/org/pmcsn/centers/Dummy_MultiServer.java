package org.pmcsn.centers;

import org.pmcsn.configuration.ConfigurationManager;
import org.pmcsn.libraries.Rngs;
import org.pmcsn.model.EventQueue;
import org.pmcsn.model.EventType;
import org.pmcsn.model.MsqEvent;
import org.pmcsn.model.MsqTime;

import static org.pmcsn.utils.Distributions.exponential;

public class Dummy_MultiServer extends MultiServer {

    private double sarrival;
    private boolean endOfArrivals;

    public Dummy_MultiServer(String centerName, double meanServiceTime, int serversNumber, int streamIndex, boolean approximateServiceAsExponential) {
        super(centerName, meanServiceTime, serversNumber, streamIndex, approximateServiceAsExponential);
    }

    @Override
    public void spawnNextCenterEvent(MsqTime time, EventQueue queue) {
        queue.add(new MsqEvent(EventType.ARRIVAL_SECOND_CENTER, time.current));
    }

    public void start(Rngs rngs, double sarrival){
        this.rngs = rngs;
        this.sarrival = sarrival;
        this.endOfArrivals = false;
        reset(rngs);
    }

    @Override
    public void spawnCompletionEvent(MsqTime time, EventQueue queue, int serverId) {
        double service = getService(streamIndex);
        //generate a new completion event
        MsqEvent event = new MsqEvent(EventType.FIRST_CENTER_DONE, time.current + service, service, serverId);
        queue.add(event);
    }

    @Override
    double getService(int streamIndex) {
        rngs.selectStream(streamIndex);
        return exponential(meanServiceTime, rngs);
    }

    public double getArrival() {
        ConfigurationManager config = new ConfigurationManager();
        rngs.selectStream(streamIndex + 1);
        sarrival += exponential(config.getDouble("general", "interArrivalTime"), rngs);
        return (sarrival);
    }

    public void generateNextArrival(EventQueue queue){
        EventType type = EventType.ARRIVAL_FIRST_CENTER;
        MsqEvent event = new MsqEvent(type, getArrival());
        queue.add(event);
    }
}

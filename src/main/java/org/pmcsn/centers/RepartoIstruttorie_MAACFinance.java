package org.pmcsn.centers;

import org.pmcsn.configuration.ConfigurationManager;
import org.pmcsn.libraries.Rngs;
import org.pmcsn.model.*;

import static org.pmcsn.utils.Distributions.exponential;

public class RepartoIstruttorie_MAACFinance extends  MultiServer{

    private double sarrival;
    private boolean endOfArrivals;

    public RepartoIstruttorie_MAACFinance(String centerName, double meanServiceTime, int serversNumber, int streamIndex, boolean approximateServiceAsExponential, boolean isImprovedModel) {
        super(centerName, meanServiceTime, serversNumber, streamIndex, approximateServiceAsExponential, isImprovedModel);
    }

    @Override
    public void spawnNextCenterEvent(MsqTime time, EventQueue queue, MsqEvent currEvent) {
        MsqEvent event = new MsqEvent(EventType.ARRIVAL_SCORING_AUTOMATICO, time.current);
        event.applicant = currEvent.applicant;
        queue.add(event);
    }

    public void start(Rngs rngs, double sarrival){
        this.rngs = rngs;
        this.sarrival = sarrival;
        this.endOfArrivals = false;
        reset(rngs);
    }

    @Override
    public void spawnCompletionEvent(MsqTime time, EventQueue queue, int serverId, MsqEvent currEvent) {
        double service = getService(streamIndex);
        //generate a new completion event
        MsqEvent event = new MsqEvent(EventType.COMPLETION_REPARTO_ISTRUTTORIE, time.current + service, service, serverId);
        event.applicant = currEvent.applicant;
        queue.add(event);
    }

    @Override
    double getService(int streamIndex) {
        rngs.selectStream(streamIndex);
        double serviceTime;
        if(approximateServiceAsExponential){
            serviceTime =  exponential(meanServiceTime, rngs);
        } else {
            // TODO: mettere il servizio effettivo
            serviceTime = 0.0;
        }
        return serviceTime;
    }

    public double getArrival() {
        ConfigurationManager config = new ConfigurationManager();
        rngs.selectStream(streamIndex + 1);
        sarrival += exponential(config.getDouble("general", "interArrivalTime"), rngs);
        return (sarrival);
    }

    public void generateNextArrival(EventQueue queue){
        EventType type = EventType.ARRIVAL_REPARTO_ISTRUTTORIE;
        MsqEvent event = new MsqEvent(type, getArrival());
        event.applicant = new Applicant();
        queue.add(event);
    }
}

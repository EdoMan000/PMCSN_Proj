package org.pmcsn.centers;

import org.pmcsn.configuration.ConfigurationManager;
import org.pmcsn.libraries.Rngs;
import org.pmcsn.model.*;

import static org.pmcsn.utils.Distributions.exponential;
import static org.pmcsn.utils.Distributions.uniform;

public class RepartoIstruttorie_MAACFinance extends  MultiServer{

    private double sarrival;
    private boolean endOfArrivals;
    private double STOP = Double.POSITIVE_INFINITY;
    private boolean isEndOfArrivals = false;

    public RepartoIstruttorie_MAACFinance(String centerName, double meanServiceTime, int serversNumber, int streamIndex, boolean approximateServiceAsExponential, boolean isImprovedModel,  boolean isBatch) {
        super(centerName, meanServiceTime, serversNumber, streamIndex, approximateServiceAsExponential, isImprovedModel, isBatch);
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
            serviceTime = uniform(meanServiceTime-2, meanServiceTime+2, rngs);
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
        double time = getArrival();

        if(time > STOP){
            isEndOfArrivals = true;

        } else {
            MsqEvent event = new MsqEvent(EventType.ARRIVAL_REPARTO_ISTRUTTORIE, time);
            event.applicant = new Applicant();
            queue.add(event);

        }
    }

    public void setStop(double stop) {
        this.STOP = stop;
    }

    public boolean isEndOfArrivals() {
        return isEndOfArrivals;
    }
}

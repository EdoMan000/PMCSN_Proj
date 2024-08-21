package org.pmcsn.centers;

import org.pmcsn.configuration.ConfigurationManager;
import org.pmcsn.libraries.Rngs;
import org.pmcsn.model.*;

import static org.pmcsn.utils.Distributions.*;

public class PreScoring_MAACFinance extends MultiServer{
    private final double sigma;
    private final double truncationPoint;
    private double sarrival;
    private boolean endOfArrivals;
    private double STOP = Double.POSITIVE_INFINITY;
    private boolean isEndOfArrivals = false;


    public PreScoring_MAACFinance(
            String centerName,
            double meanServiceTime,
            double sigma,
            double truncationPoint,
            int serversNumber,
            int streamIndex,
            boolean approximateServiceAsExponential,
            boolean isBatch,
            int batchSize,
            int numBatches) {
        super(centerName, meanServiceTime, serversNumber, streamIndex, approximateServiceAsExponential, isBatch, batchSize, numBatches);
        this.sigma = sigma;
        this.truncationPoint = truncationPoint;
    }

    @Override
    public void spawnNextCenterEvent(MsqTime time, EventQueue queue, MsqEvent currEvent) {
        if(currEvent.applicant.isAcceptedByPreScoring()) {
            MsqEvent event = new MsqEvent(EventType.ARRIVAL_REPARTO_ISTRUTTORIE, time.current);
            event.applicant = currEvent.applicant;
            queue.add(event);
            if(!isBatch || (!warmup && !isDone())) acceptedJobs++;
        }
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
        MsqEvent event = new MsqEvent(EventType.COMPLETION_PRE_SCORING, time.current + service, service, serverId);
        if (currEvent.type == EventType.ARRIVAL_PRE_SCORING) {
            event.applicant = currEvent.applicant;
        }
        queue.add(event);
    }

    @Override
    double getService(int streamIndex) {
        rngs.selectStream(streamIndex);
        double serviceTime;
        if(approximateServiceAsExponential){
            serviceTime = exponential(meanServiceTime, rngs);
        } else {
            serviceTime = truncatedLogNormal(meanServiceTime, sigma, truncationPoint, rngs);
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
            MsqEvent event = new MsqEvent(EventType.ARRIVAL_PRE_SCORING, time);
            event.applicant = Applicant.create(rngs);
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

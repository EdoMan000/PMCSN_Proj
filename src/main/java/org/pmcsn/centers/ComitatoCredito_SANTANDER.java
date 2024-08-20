package org.pmcsn.centers;

import org.pmcsn.model.*;

import static org.pmcsn.utils.Distributions.*;

public class ComitatoCredito_SANTANDER extends InfiniteServer {
    private final boolean isImprovedSimulation;
    public int feedback = 0;
    public int feedbackCreated = 0;

    public ComitatoCredito_SANTANDER(String centerName, double meanServiceTime, int streamIndex, boolean approximateServiceAsExponential, boolean isBatch, int batchSize, int numBatches, boolean isImprovedSimulation) {
        super(centerName, meanServiceTime, streamIndex, approximateServiceAsExponential, isBatch, batchSize, numBatches);
        this.isImprovedSimulation = isImprovedSimulation;
    }

    @Override
    public void spawnNextCenterEvent(MsqTime time, EventQueue queue, MsqEvent currEvent) {
        MsqEvent event;
        switch (currEvent.applicant.getNextRoute()) {
            case -1:
                event = new MsqEvent(EventType.ARRIVAL_REPARTO_ISTRUTTORIE, time.current);
                if (isImprovedSimulation) {
                    event.applicant = currEvent.applicant.improvedFeedback(rngs);
                } else {
                    event.applicant = currEvent.applicant.feedback(rngs);
                }
                queue.add(event);
                if (!isBatch || (!warmup && !isDone())) acceptedJobs++;
                break;
            case 0:
                event = new MsqEvent(EventType.ARRIVAL_REPARTO_LIQUIDAZIONI, time.current);
                event.applicant = currEvent.applicant;
                queue.add(event);
                break;
            case 1:
                break;
            default:
                throw new IllegalArgumentException("Invalid route " + currEvent.applicant.getNextRoute());
        }
    }

    @Override
    public void spawnCompletionEvent(MsqTime time, EventQueue queue, MsqEvent currEvent) {
        double service = getService(streamIndex);
        MsqEvent event = new MsqEvent(EventType.COMPLETION_COMITATO_CREDITO, time.current + service, service);
        event.applicant = currEvent.applicant;
        queue.add(event);
    }

    protected double getService(int streamIndex) {
        rngs.selectStream(streamIndex);
        double serviceTime;
        if(approximateServiceAsExponential){
            serviceTime = exponential(meanServiceTime, rngs);
        } else {
            serviceTime = uniform(meanServiceTime-60, meanServiceTime+60, rngs);
        }
        return serviceTime;
    }
}

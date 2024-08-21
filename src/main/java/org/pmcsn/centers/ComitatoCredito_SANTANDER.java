package org.pmcsn.centers;

import org.pmcsn.libraries.Rngs;
import org.pmcsn.model.*;

import static org.pmcsn.utils.Distributions.*;

public class ComitatoCredito_SANTANDER extends InfiniteServer {
    enum Route {
        FEEDBACK,
        ACCEPTED,
        REJECTED
    }
    private final boolean isImprovedSimulation;
    private final double sigma;
    private final double truncationPoint;
    public int feedback = 0;
    public int feedbackCreated = 0;

    public ComitatoCredito_SANTANDER(
            String centerName,
            double meanServiceTime,
            double sigma,
            double truncationPoint,
            int streamIndex,
            boolean approximateServiceAsExponential,
            boolean isBatch,
            int batchSize,
            int numBatches,
            boolean isImprovedSimulation) {
        super(centerName, meanServiceTime, streamIndex, approximateServiceAsExponential, isBatch, batchSize, numBatches);
        this.sigma = sigma;
        this.truncationPoint = truncationPoint;
        this.isImprovedSimulation = isImprovedSimulation;
    }

    @Override
    public void spawnNextCenterEvent(MsqTime time, EventQueue queue, MsqEvent currEvent) {
        MsqEvent event;
        switch (getNextRoute()) {
            case FEEDBACK:
                event = new MsqEvent(EventType.ARRIVAL_REPARTO_ISTRUTTORIE, time.current);
                if (isImprovedSimulation) {
                    event.applicant = currEvent.applicant.improvedFeedback(rngs);
                } else {
                    event.applicant = currEvent.applicant.feedback(rngs);
                }
                queue.add(event);
                if (!isBatch || (!warmup && !isDone())) acceptedJobs++;
                break;
            case ACCEPTED:
                event = new MsqEvent(EventType.ARRIVAL_REPARTO_LIQUIDAZIONI, time.current);
                event.applicant = currEvent.applicant;
                queue.add(event);
                break;
            case REJECTED:
                break;
        }
    }

    private Route getNextRoute() {
        rngs.selectStream(streamIndex + 1);
        double x = rngs.random();
        if (x < 0.06) {
            return Route.FEEDBACK;
        } else if (x >= 0.06 && x < 0.71) {
            return Route.ACCEPTED;
        } else {
            return Route.REJECTED;
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
            serviceTime = truncatedLogNormal(meanServiceTime, sigma, truncationPoint, rngs);
        }
        return serviceTime;
    }
}

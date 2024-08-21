package org.pmcsn.centers;

import org.pmcsn.model.EventQueue;
import org.pmcsn.model.EventType;
import org.pmcsn.model.MsqEvent;
import org.pmcsn.model.MsqTime;

import static org.pmcsn.utils.Distributions.uniform;

import static org.pmcsn.utils.Distributions.exponential;

public class SysScoringAutomatico_SANTANDER extends SingleServer {
    private final boolean isImprovedSimulation;

    public SysScoringAutomatico_SANTANDER(
            String centerName,
            double meanServiceTime,
            int streamIndex,
            boolean approximateServiceAsExponential,
            boolean isBatch,
            boolean isImprovedSimulation,
            int batchSize,
            int numBatches) {
        super(centerName, meanServiceTime, streamIndex, approximateServiceAsExponential, isBatch, batchSize, numBatches);
        this.isImprovedSimulation = isImprovedSimulation;
    }

    @Override
    public void spawnCompletionEvent(MsqTime time, EventQueue queue, MsqEvent currEvent) {
        double service = getService(streamIndex);
        MsqEvent event = new MsqEvent(EventType.COMPLETION_SCORING_AUTOMATICO, time.current + service, service);
        if (currEvent.type == EventType.ARRIVAL_SCORING_AUTOMATICO) {
            event.applicant = currEvent.applicant;
        }
        queue.add(event);
    }

    @Override
    public void spawnNextCenterEvent(MsqTime time, EventQueue queue, MsqEvent currEvent) {
        rngs.selectStream(streamIndex + 1);
        currEvent.applicant.setHasCorrispondenzaInBancaDati(rngs);
        if (isImprovedSimulation && currEvent.applicant.isAcceptedBySysScoring()) {
            baseSpawnNextCenterEvent(time, queue, currEvent);
        } else if (!isImprovedSimulation && currEvent.applicant.isAcceptedBySysScoring()){
            baseSpawnNextCenterEvent(time, queue, currEvent);
        }
    }

    private void baseSpawnNextCenterEvent(MsqTime time, EventQueue queue, MsqEvent currEvent) {
        EventType type = EventType.ARRIVAL_COMITATO_CREDITO;
        MsqEvent event = new MsqEvent(type, time.current);
        event.applicant = currEvent.applicant;
        queue.add(event);
        if (!isBatch || (!warmup && !isDone())) acceptedJobs++;
    }

     @Override
    protected double getService(int streamIndex) {
        rngs.selectStream(streamIndex);
        double serviceTime;
        serviceTime = exponential(meanServiceTime, rngs);
        return serviceTime;
    }
}

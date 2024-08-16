package org.pmcsn.centers;

import org.pmcsn.model.EventQueue;
import org.pmcsn.model.EventType;
import org.pmcsn.model.MsqEvent;
import org.pmcsn.model.MsqTime;

import static org.pmcsn.utils.Distributions.uniform;

import static org.pmcsn.utils.Distributions.exponential;
import static org.pmcsn.utils.ProbabilitiesUtils.isAcceptedSysScoring;

public class SysScoringAutomatico_SANTANDER extends SingleServer {

    public SysScoringAutomatico_SANTANDER(String centerName, double meanServiceTime, int streamIndex, boolean approximateServiceAsExponential, boolean isDigitalSignature,  boolean isBatch) {
        super(centerName, meanServiceTime, streamIndex, approximateServiceAsExponential, isDigitalSignature, isBatch);
    }

    @Override
    public void spawnNextCenterEvent(MsqTime time, EventQueue queue, MsqEvent currEvent) {
        // if (isAcceptedSysScoring(rngs, streamIndex)) {
        /*rngs.selectStream(streamIndex + 3);
        double p = rngs.random();
        if (p < 0.80) {

         */
        if(currEvent.applicant.hasCorrespondingData()){
            EventType type = EventType.ARRIVAL_COMITATO_CREDITO;
            MsqEvent event = new MsqEvent(type, time.current);
            event.isFeedback = currEvent.isFeedback;
            queue.add(event);
            if( !isBatch || (!warmup && !isDone())) acceptedJobs++;
        }
    }

    @Override
    public void spawnCompletionEvent(MsqTime time, EventQueue queue, MsqEvent currEvent) {
        double service = getService(streamIndex);
        MsqEvent event = new MsqEvent(EventType.COMPLETION_SCORING_AUTOMATICO, time.current + service, service);
        event.applicant = currEvent.applicant;
        event.isFeedback = currEvent.isFeedback;
        queue.add(event);
    }

    protected double getService(int streamIndex) {
        rngs.selectStream(streamIndex);
        double serviceTime;
        if(approximateServiceAsExponential){
            serviceTime = exponential(meanServiceTime, rngs);
        } else {
            serviceTime = uniform(meanServiceTime-0.5, meanServiceTime+0.5, rngs);
        }
        return serviceTime;
    }
}

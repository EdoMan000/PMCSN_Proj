package org.pmcsn.centers;

import org.pmcsn.model.EventQueue;
import org.pmcsn.model.EventType;
import org.pmcsn.model.MsqEvent;
import org.pmcsn.model.MsqTime;

import static org.pmcsn.utils.Distributions.uniform;
import static org.pmcsn.utils.ProbabilitiesUtils.*;

import static org.pmcsn.utils.Distributions.exponential;

public class SysScoringAutomatico_SANTANDER extends  SingleServer {

    public SysScoringAutomatico_SANTANDER(String centerName, double meanServiceTime, int streamIndex, boolean approximateServiceAsExponential, boolean isDigitalSignature,  boolean isBatch) {
        super(centerName, meanServiceTime, streamIndex, approximateServiceAsExponential, isDigitalSignature, isBatch);
    }

    @Override
    public void spawnNextCenterEvent(MsqTime time, EventQueue queue, MsqEvent currEvent) {
        //if(isAcceptedSysScoring(rngs, streamindex)){
        if(currEvent.applicant.hasValidaData() && currEvent.applicant.hasCorrespondingData()){
            EventType type = EventType.ARRIVAL_COMITATO_CREDITO;
            MsqEvent event = new MsqEvent(type, time.current);
            queue.add(event);

            if( !isBatch || (!warmup && !isDone())) acceptedJobs++;
        }

    }

    @Override
    public void spawnCompletionEvent(MsqTime time, EventQueue queue, MsqEvent currEvent) {
        double service = getService(streamindex);
        MsqEvent event = new MsqEvent(EventType.COMPLETION_SCORING_AUTOMATICO, time.current + service, service);
        event.applicant = currEvent.applicant;
        queue.add(event);
    }

    protected double getService(int streamIndex) {
        rngs.selectStream(streamIndex);
        double serviceTime;
        if(approximateServiceAsExponential){
            serviceTime =  exponential(meanServiceTime, rngs);
        } else {
            serviceTime = uniform(meanServiceTime-0.5, meanServiceTime+0.5, rngs);
        }
        return serviceTime;
    }
}

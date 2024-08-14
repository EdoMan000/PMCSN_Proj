package org.pmcsn.centers;

import org.pmcsn.model.*;

import static org.pmcsn.utils.Distributions.uniform;

import static org.pmcsn.utils.Distributions.exponential;

public class ComitatoCredito_SANTANDER extends SingleServer {
    public int feedback = 0;
    public int feedbackCreated = 0;

    public ComitatoCredito_SANTANDER(String centerName, double meanServiceTime, int streamIndex, boolean approximateServiceAsExponential, boolean isDigitalSignature, boolean isBatch) {
        super(centerName, meanServiceTime, streamIndex, approximateServiceAsExponential, isDigitalSignature, isBatch);
    }

    @Override
    public void processCompletion(MsqEvent completion, MsqTime time, EventQueue queue) {
        super.processCompletion(completion, time, queue);
        if (completion.isFeedback) {
            feedback++;
        }
    }

    @Override
    public void spawnNextCenterEvent(MsqTime time, EventQueue queue, MsqEvent currEvent) {
        rngs.selectStream(streamIndex + 1);
        double p = rngs.random();
        //if (p >= 0.06 && p < 0.71) {
        if (p >= 0.06 && p < 0.71) {
            EventType type = EventType.ARRIVAL_REPARTO_LIQUIDAZIONI;
            MsqEvent event = new MsqEvent(type, time.current);
            event.applicant = currEvent.applicant;
            queue.add(event);
            if(!warmup && !isDone()) acceptedJobs++;
        } else if (p >= 0 && p < 0.06) {
            EventType type = EventType.ARRIVAL_REPARTO_ISTRUTTORIE;
            MsqEvent event = new MsqEvent(type, time.current);
            //TODO: ci va applicant vecchio?
            event.applicant = new Applicant(rngs);
            event.isFeedback = true;
            if(!warmup && !isDone()) feedbackCreated++;
            queue.add(event);
        }
    }

    @Override
    public void spawnCompletionEvent(MsqTime time, EventQueue queue, MsqEvent currEvent) {
        double service = getService(streamIndex);
        MsqEvent event = new MsqEvent(EventType.COMPLETION_COMITATO_CREDITO, time.current + service, service);
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
            serviceTime = uniform(meanServiceTime-60, meanServiceTime+60, rngs);
        }
        return serviceTime;
    }
}

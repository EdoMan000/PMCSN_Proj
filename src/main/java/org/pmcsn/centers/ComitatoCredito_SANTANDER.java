package org.pmcsn.centers;

import org.pmcsn.model.*;

import static org.pmcsn.utils.Distributions.uniform;
import static org.pmcsn.utils.ProbabilitiesUtils.*;

import static org.pmcsn.utils.Distributions.exponential;

public class ComitatoCredito_SANTANDER extends SingleServer {

    public ComitatoCredito_SANTANDER(String centerName, double meanServiceTime, int streamIndex, boolean approximateServiceAsExponential, boolean isImprovedModel, boolean isBatch) {
        super(centerName, meanServiceTime, streamIndex, approximateServiceAsExponential, isImprovedModel, isBatch);
    }

    @Override
    public void spawnNextCenterEvent(MsqTime time, EventQueue queue, MsqEvent currEvent) {
        if(isAccepetdComitato(rngs, streamindex)){
            EventType type = EventType.ARRIVAL_REPARTO_LIQUIDAZIONI;
            MsqEvent event = new MsqEvent(type, time.current);
            event.applicant = currEvent.applicant;
            queue.add(event);
            if(!warmup && !isDone()) acceptedJobs++;
        }else if(isFeedback(rngs, streamindex)){
            EventType type = EventType.ARRIVAL_REPARTO_ISTRUTTORIE;
            MsqEvent event = new MsqEvent(type, time.current);
            event.applicant = new Applicant();
            queue.add(event);
        }
    }

    @Override
    public void spawnCompletionEvent(MsqTime time, EventQueue queue, MsqEvent currEvent) {
        double service = getService(streamindex);
        MsqEvent event = new MsqEvent(EventType.COMPLETION_COMITATO_CREDITO, time.current + service, service);
        event.applicant = currEvent.applicant;
        queue.add(event);
    }

    protected double getService(int streamIndex) {
        rngs.selectStream(streamIndex);
        double serviceTime;
        if(approximateServiceAsExponential){
            serviceTime =  exponential(meanServiceTime, rngs);
        } else {
            // TODO: mettere il servizio effettivo
            serviceTime = uniform(meanServiceTime-60, meanServiceTime+60, rngs);
        }
        return serviceTime;
    }
}

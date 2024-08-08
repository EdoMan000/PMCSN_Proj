package org.pmcsn.centers;

import org.pmcsn.model.EventQueue;
import org.pmcsn.model.EventType;
import org.pmcsn.model.MsqEvent;
import org.pmcsn.model.MsqTime;

import static org.pmcsn.utils.Distributions.exponential;

public class RepartoLiquidazioni_MAACFinance extends SingleServer {

    public RepartoLiquidazioni_MAACFinance(String centerName, double meanServiceTime, int streamIndex, boolean approximateServiceAsExponential, boolean isImprovedModel) {
        super(centerName, meanServiceTime, streamIndex, approximateServiceAsExponential, isImprovedModel);
    }

    @Override
    public void spawnNextCenterEvent(MsqTime time, EventQueue queue, MsqEvent currEvent) {

    }

    @Override
    public void spawnCompletionEvent(MsqTime time, EventQueue queue, MsqEvent currEvent) {
        double service = getService(streamindex);
        MsqEvent event = new MsqEvent(EventType.COMPLETION_REPARTO_LIQUIDAZIONI, time.current + service, service);
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
            serviceTime = 0.0;
        }
        return serviceTime;
    }
}
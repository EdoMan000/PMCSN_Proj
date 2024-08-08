package org.pmcsn.centers;

import org.pmcsn.model.EventQueue;
import org.pmcsn.model.EventType;
import org.pmcsn.model.MsqEvent;
import org.pmcsn.model.MsqTime;
import static org.pmcsn.utils.ProbabilitiesUtils.*;

import static org.pmcsn.utils.Distributions.exponential;

public class ComitatoCredito_SANTANDER extends SingleServer {

    public ComitatoCredito_SANTANDER(String centerName, double meanServiceTime, int streamIndex, boolean approximateServiceAsExponential) {
        super(centerName, meanServiceTime, streamIndex, approximateServiceAsExponential);
    }

    @Override
    public void spawnNextCenterEvent(MsqTime time, EventQueue queue) {
        if(isAccepetdComitato(rngs, streamindex)){
            EventType type = EventType.ARRIVAL_REPARTO_LIQUIDAZIONI;
            MsqEvent event = new MsqEvent(type, time.current);
            queue.add(event);
        }
    }

    @Override
    public void spawnCompletionEvent(MsqTime time, EventQueue queue) {
        double service = getService(streamindex);
        MsqEvent event = new MsqEvent(EventType.COMPLETION_COMITATO_CREDITO, time.current + service, service);
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

package org.pmcsn.centers;

import org.pmcsn.model.*;

import java.util.ArrayList;
import java.util.List;

import static org.pmcsn.utils.Distributions.*;

public class RepartoLiquidazioni_MAACFinance extends SingleServer {
    private final double sigma;
    private final double truncationPoint;
    private final List<Double> residenceTimes = new ArrayList<>();

    public RepartoLiquidazioni_MAACFinance(
            String centerName,
            double meanServiceTime,
            double sigma,
            double truncationPoint,
            int streamIndex,
            boolean approximateServiceAsExponential,
            boolean isBatch,
            int batchSize,
            int numBatches) {
        super(centerName, meanServiceTime, streamIndex, approximateServiceAsExponential, isBatch, batchSize, numBatches);
        this.sigma = sigma;
        this.truncationPoint = truncationPoint;
    }

    @Override
    public void spawnNextCenterEvent(MsqTime time, EventQueue queue, MsqEvent currEvent) {
        residenceTimes.add(currEvent.getTime() - currEvent.applicant.getEntranceTime());
    }

    @Override
    public void spawnCompletionEvent(MsqTime time, EventQueue queue, MsqEvent currEvent) {
        double service = getService(streamIndex);
        MsqEvent event = new MsqEvent(EventType.COMPLETION_REPARTO_LIQUIDAZIONI, time.current + service, service);
        if (currEvent.type == EventType.ARRIVAL_REPARTO_LIQUIDAZIONI) {
            event.applicant = currEvent.applicant;
        }
        queue.add(event);
    }

//    @Override
//    public void updateObservations(Observations observations) {
//        observations.saveObservation(getMeanResidenceTime());
//    }

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

    public List<Double> getResidenceTimes() {
        return residenceTimes;
    }

    public double getMeanResidenceTime() {
        return residenceTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
}

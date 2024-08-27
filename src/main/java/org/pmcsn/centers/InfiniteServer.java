package org.pmcsn.centers;

import org.pmcsn.libraries.Rngs;
import org.pmcsn.model.*;

import java.util.List;

import static org.pmcsn.model.MeanStatistics.computeMean;
import static org.pmcsn.utils.PrintUtils.*;

public abstract class InfiniteServer {
    protected int streamIndex;
    protected BasicStatistics statistics;
    protected BatchStatistics batchStatistics;
    protected final Area area = new Area();
    protected double meanServiceTime;
    protected long numberOfJobsInNode = 0;
    protected long totalNumberOfJobsServed = 0;
    protected double firstArrivalTime = Double.NEGATIVE_INFINITY;
    protected double lastArrivalTime = 0;
    protected double lastCompletionTime = 0;
    protected String centerName;
    protected Rngs rngs;
    protected MsqSum sum = new MsqSum();
    protected boolean approximateServiceAsExponential;
    protected int batchSize;
    protected double currentBatchStartTime = 0;
    protected boolean warmup = true;
    protected int numBatch = 0;
    protected long jobServedPerBatch = 0;
    protected float acceptedJobs = 0 ;
    protected float totJobs = 0;
    protected boolean isBatch;

    public InfiniteServer(String centerName, double meanServiceTime, int streamIndex, boolean approximateServiceAsExponential, boolean isBatch, int batchSize, int numBatches) {
        this.batchSize = batchSize;
        this.centerName = centerName;
        this.meanServiceTime = meanServiceTime;
        this.streamIndex = streamIndex;
        this.statistics = new BasicStatistics(centerName);
        this.batchStatistics = new BatchStatistics(centerName, numBatches);
        this.approximateServiceAsExponential = approximateServiceAsExponential;
        this.isBatch = isBatch;
    }

    //********************************** ABSTRACT METHODS *********************************************
    protected abstract void spawnNextCenterEvent(MsqTime time, EventQueue queue, MsqEvent currEvent);
    protected abstract void spawnCompletionEvent(MsqTime time, EventQueue queue, MsqEvent currEvent);
    protected abstract double getService(int streamIndex);

    //********************************** CONCRETE METHODS *********************************************

    public void reset(Rngs rngs) {
        this.rngs = rngs;
        area.reset();
        sum.reset();
        this.numberOfJobsInNode = 0;
        this.firstArrivalTime = Double.NEGATIVE_INFINITY;
        this.lastArrivalTime = 0;
        this.lastCompletionTime = 0;
        this.acceptedJobs = 0;
        this.totJobs = 0;
    }

    public long getNumberOfJobsInNode() {
        return numberOfJobsInNode;
    }

    public long getTotalNumberOfJobsServed(){
        return totalNumberOfJobsServed;
    }

    public void setArea(MsqTime time) {
        if (numberOfJobsInNode > 0) {
            double width = time.next - time.current;
            area.incNodeArea(width * numberOfJobsInNode);
            area.incServiceArea(width);
        }
    }

    public void processArrival(MsqEvent arrival, MsqTime time, EventQueue queue){
        // increment the number of jobs in the node
        numberOfJobsInNode++;

        // Updating the first arrival time (we will use it in the statistics)
        if(firstArrivalTime == Double.NEGATIVE_INFINITY) {
            firstArrivalTime = arrival.time;
        }
        lastArrivalTime = arrival.time;

        spawnCompletionEvent(time, queue, arrival);
    }

    public void processCompletion(MsqEvent completion, MsqTime time, EventQueue queue) {
        numberOfJobsInNode--;

        if(!isDone()){
            totalNumberOfJobsServed++;
            jobServedPerBatch++;
        }

        sum.served++;
        sum.service += completion.service;
        lastCompletionTime = completion.time;

        // If not in warm up then saving the statistics (OF CURRENT BATCH!!!)
        if (!warmup && jobServedPerBatch == batchSize ) {
            saveBatchStats(time);
        }
        spawnNextCenterEvent(time, queue, completion);

        if(!isBatch || (!warmup && !isDone())) totJobs++;
    }

    public void resetBatch(MsqTime time) {
        area.reset();
        sum.reset();
        numBatch++;
        jobServedPerBatch = 0;
        currentBatchStartTime = time.current;
    }


    public void saveStats() {
        MsqSum[] sums = new MsqSum[1];
        sums[0] = this.sum;
        statistics.saveStats(area, sums, lastArrivalTime, lastCompletionTime, false, currentBatchStartTime);
        statistics.addProbAccept(acceptedJobs / totJobs);
        statistics.addJobServed(totJobs);
        statistics.addBusyTime(getBusyTime());
    }

    public void writeStats(String simulationType, long seed) {
        statistics.writeStats(simulationType, seed);
        List<Double> prob = statistics.getProbAccept();
        List<Double> totJobsList = statistics.getJobServed();

        // Compute the necessary values
        double avgAcceptanceRate = prob.isEmpty() ? 0 : computeMean(prob);
        double avgJobServed = computeMean(totJobsList);

        // Print all the stats
        printStats(centerName, avgAcceptanceRate, avgJobServed, statistics.getMeanStatistics().meanServiceTime, statistics.getMeanBusyTime());
    }

    public MeanStatistics getMeanStatistics() {
        return statistics.getMeanStatistics();
    }

    public BasicStatistics getStatistics() {
        return this.statistics;
    }

    public void writeBatchStats(String simulationType, long seed){
        batchStatistics.writeStats(simulationType, seed);
        System.out.println(centerName +" Probability is " + acceptedJobs/totJobs);
    }


    public MeanStatistics getBatchMeanStatistics() {
        return batchStatistics.getMeanStatistics();
    }

    public BatchStatistics getBatchStatistics() {
        return batchStatistics;
    }

    public void saveBatchStats(MsqTime time) {
        // the number of jobs served cannot be 0 since the method is invoked in processCompletion()
        MsqSum[] s = new MsqSum[1];
        s[0] = sum;
        batchStatistics.saveStats(area, s, lastArrivalTime, lastCompletionTime, false, currentBatchStartTime);
        resetBatch(time);
    }

    public void stopWarmup(MsqTime time) {
        this.warmup = false;
        resetBatch(time);
    }

    public boolean isDone() {
        return batchStatistics.isBatchRetrievalDone();
    }

    public void updateObservations(Observations observations) {
        if (lastArrivalTime == 0 || lastCompletionTime == 0) {
            observations.saveObservation(0);
            return;
        }
        double meanResponseTime = area.getNodeArea() / sum.served;
        observations.saveObservation(meanResponseTime);
    }

    public float getTotalNumberOfJobs() {
        return totJobs;
    }

    public float getAcceptedJobs() {
        return acceptedJobs;
    }

    public String getCenterName() {
        return centerName;
    }

    public double getBusyTime() {
        return sum.service;
    }
}

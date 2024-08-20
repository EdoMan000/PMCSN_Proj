package org.pmcsn.centers;


import org.pmcsn.libraries.Rngs;
import org.pmcsn.model.*;

import java.util.*;

import static org.pmcsn.model.MeanStatistics.computeMean;
import static org.pmcsn.utils.PrintUtils.*;

public abstract class MultiServer {
    protected long numberOfJobsInNode = 0;
    protected long totalNumberOfJobsServed = 0;
    protected int SERVERS;
    protected int streamIndex;
    protected Area area;
    protected double firstArrivalTime = Double.NEGATIVE_INFINITY;
    protected double lastArrivalTime = 0;
    protected double lastCompletionTime = 0;
    protected double meanServiceTime;
    protected String centerName;
    protected boolean approximateServiceAsExponential;
    protected Rngs rngs;
    protected int batchSize;
    private double currentBatchStartTime;
    protected MsqSum[] sum;
    protected MsqServer[] servers;
    protected BasicStatistics statistics;
    protected BatchStatistics batchStatistics;
    protected long jobServedPerBatch = 0;
    protected boolean warmup = true;
    protected boolean isBatch;
    protected float acceptedJobs = 0 ;
    protected float totJobs = 0;
    protected Deque<Applicant> info;


    public MultiServer(String centerName, double meanServiceTime, int serversNumber, int streamIndex, boolean approximateServiceAsExponential, boolean isBatch, int batchSize, int numBatches) {
        this.batchSize = batchSize;
        this.centerName = centerName;
        this.meanServiceTime = meanServiceTime;
        this.SERVERS = serversNumber;
        this.streamIndex = streamIndex;
        this.sum =  new MsqSum[SERVERS];
        this.servers = new MsqServer[SERVERS];
        this.info = new ArrayDeque<>();
        for(int i=0; i<SERVERS ; i++){
            sum[i] = new MsqSum();
            servers[i] = new MsqServer();
        }
        this.area = new Area();
        this.statistics = new BasicStatistics(centerName);
        this.batchStatistics = new BatchStatistics(centerName, numBatches);
        this.approximateServiceAsExponential = approximateServiceAsExponential;
        this.isBatch = isBatch;
    }

    //********************************** ABSTRACT METHODS *********************************************
    abstract void spawnNextCenterEvent(MsqTime time, EventQueue queue, MsqEvent currEvent);
    abstract void spawnCompletionEvent(MsqTime time, EventQueue queue, int serverId, MsqEvent currEvent);
    abstract double getService(int streamIndex);

    public void stopWarmup(MsqTime time) {
        this.warmup = false;
        resetBatch(time);
    }

    //********************************** CONCRETE METHODS *********************************************
    public void reset(Rngs rngs) {
        this.rngs = rngs;
        // resetting variables
        this.numberOfJobsInNode =0;
        area.reset();
        this.firstArrivalTime = Double.NEGATIVE_INFINITY;
        this.lastArrivalTime = 0;
        this.lastCompletionTime = 0;
        for(int i=0; i<SERVERS ; i++){
            sum[i].reset();
            servers[i].reset();
        }
        this.acceptedJobs = 0;
        this.totJobs = 0;
    }

    public void resetBatch(MsqTime time) {
        area.reset();
        Arrays.stream(sum).forEach(MsqSum::reset);
        jobServedPerBatch = 0;
        currentBatchStartTime = time.current;
    }

    public long getTotalNumberOfJobsServed(){
        return totalNumberOfJobsServed;
    }

    public BasicStatistics getStatistics(){
        return statistics;
    }

    public BatchStatistics getBatchStatistics() {
        return batchStatistics;
    }

    public long getNumberOfJobsInNode() {
        return numberOfJobsInNode;
    }

    public void setArea(MsqTime time) {
        double width = time.next - time.current;
        area.incNodeArea(width * numberOfJobsInNode);
        long busyServers = Arrays.stream(servers).filter(x -> x.running).count();
        area.incQueueArea(width * (numberOfJobsInNode - busyServers));
        area.incServiceArea(width);
    }

    public void processArrival(MsqEvent arrival, MsqTime time, EventQueue queue){
        // increment the number of jobs in the node
        numberOfJobsInNode++;

        // Updating the first arrival time (we will use it in the statistics)
        if(firstArrivalTime == Double.NEGATIVE_INFINITY){
            firstArrivalTime = arrival.time;
        }
        lastArrivalTime = arrival.time;
        if (numberOfJobsInNode <= SERVERS) {
            int serverId = findOne();
            servers[serverId].running = true;
            spawnCompletionEvent(time, queue, serverId, arrival);
        } else {
            info.addFirst(arrival.applicant);
        }
    }

    public void processCompletion(MsqEvent completion, MsqTime time, EventQueue queue) {
        numberOfJobsInNode--;

        if(!isDone()){
            totalNumberOfJobsServed++;
            jobServedPerBatch++;
        }

        int serverId = completion.serverId;
        sum[serverId].service += completion.service;
        sum[serverId].served++;
        lastCompletionTime = completion.time;
        if (!warmup && jobServedPerBatch == batchSize) {
            saveBatchStats(time);
        }
        if (completion.applicant == null) {
            completion.applicant = info.poll();
        }
        spawnNextCenterEvent(time, queue, completion);
        if (numberOfJobsInNode >= SERVERS) {
            spawnCompletionEvent(time, queue, serverId, completion);
        } else {
            servers[serverId].lastCompletionTime = completion.time;
            servers[serverId].running = false;
        }

        if(!isBatch || (!warmup && !isDone())) totJobs++;
    }

    public int findOne() {
        int s;
        int i = 0;
        while (servers[i].running)       /* find the index of the first available */
            i++;                        /* (idle) server                         */
        s = i;
        if (s == SERVERS) return s;
        while (i < SERVERS-1) {         /* now, check the others to find which   */
            i++;                        /* has been idle longest                 */
            if (!servers[i].running && (servers[i].lastCompletionTime < servers[s].lastCompletionTime))
                s = i;
        }
        return s;
    }

    public void saveStats() {
        statistics.saveStats(area, sum, lastArrivalTime, lastCompletionTime, true);
        if(centerName.contains("SCORING")) {
            statistics.addProbAccept(acceptedJobs / totJobs);
        }
        statistics.addJobServed(totJobs);
    }

    public void writeStats(String simulationType) {
        statistics.writeStats(simulationType);
        List<Double> prob = statistics.getProbAccept();
        List<Double> totJobsList = statistics.getJobServed();

        // Compute the necessary values
        double avgAcceptanceRate = prob.isEmpty() ? 0 : computeMean(prob);
        double avgJobServed = computeMean(totJobsList);

        // Print all the stats
        printStats(centerName, avgAcceptanceRate, avgJobServed, meanServiceTime);
    }

    public MeanStatistics getMeanStatistics() {
        return statistics.getMeanStatistics();
    }

    public void writeBatchStats(String simulationType){
        batchStatistics.writeStats(simulationType);
    }

    public void saveBatchStats(MsqTime time) {
        // the number of jobs served cannot be 0 since the method is invoked in processCompletion()
        batchStatistics.saveStats(area, sum, lastArrivalTime, lastCompletionTime, true, currentBatchStartTime);
        resetBatch(time);
    }

    public MeanStatistics getBatchMeanStatistics() {
        return batchStatistics.getMeanStatistics();
    }

    public void updateObservations(List<Observations> observationsList, int run) {
        for (int i = 0; i < observationsList.size(); i++) {
            updateObservation(observationsList.get(i), run, i);
        }
    }

    private void updateObservation(Observations observations, int run, int serverId) {
        long numberOfJobsServed = 0;
        for (MsqSum s : sum) {
            numberOfJobsServed += s.served;
        }
        if (lastArrivalTime == 0 || numberOfJobsServed == 0 || servers[serverId].lastCompletionTime == 0.0) {
            return;
        }
        double lambda = numberOfJobsServed / lastArrivalTime;
        double meanNodePopulation = area.getNodeArea() / servers[serverId].lastCompletionTime;
        double meanResponseTime = meanNodePopulation / lambda;
        observations.saveObservation(run, Observations.INDEX.RESPONSE_TIME, meanResponseTime);
    }

    public boolean isDone() {
        return batchStatistics.isBatchRetrievalDone();
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

    public int getServersNumber() {
        return servers.length;
    }
}

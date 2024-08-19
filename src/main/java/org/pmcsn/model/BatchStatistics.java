package org.pmcsn.model;

import java.util.List;

public class BatchStatistics extends AbstractStatistics {
    private int batchRetrievalDone = 0;
    private final int numBatches;

    public BatchStatistics(String centerName, int numBatches) {
        super(centerName);
        this.numBatches = numBatches;
    }

    @Override
    void add(Index index, List<Double> list, double value) {
        list.add(value);
        if(list.size() >= numBatches) {
            batchRetrievalDone++;
        }
    }

    public boolean isBatchRetrievalDone() {
        return batchRetrievalDone == 7;
    }

}

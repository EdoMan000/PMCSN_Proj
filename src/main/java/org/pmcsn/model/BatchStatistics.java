package org.pmcsn.model;

import java.util.List;

public class BatchStatistics extends AbstractStatistics {
    private boolean batchRetrievalDone = false;
    private final int batchesNumber;

    public BatchStatistics(String centerName, int batchesNumber) {
        super(centerName);
        this.batchesNumber = batchesNumber;
    }

    @Override
    void add(Index index, List<Double> list, double value) {
        list.add(value);
        if(list.size() >= batchesNumber) {
            batchRetrievalDone = true;
        }
    }

    public boolean isBatchRetrievalDone() {
        return batchRetrievalDone;
    }

}

package org.pmcsn.model;

import java.util.List;

public class BatchStatistics extends AbstractStatistics {
    private int batchRetrievalDone = 0;
    private final int batchesNumber;

    public BatchStatistics(String centerName, int batchesNumber) {
        super(centerName);
        this.batchesNumber = batchesNumber;
    }

    @Override
    void add(Index index, List<Double> list, double value) {
        list.add(value);
        if(list.size() >= batchesNumber) {
            batchRetrievalDone++;
        }
    }

    public boolean isBatchRetrievalDone() {
        return batchRetrievalDone == 7;
    }

}

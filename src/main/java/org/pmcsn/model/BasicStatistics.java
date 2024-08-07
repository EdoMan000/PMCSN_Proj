package org.pmcsn.model;

import java.util.List;

public class BasicStatistics extends AbstractStatistics {
    public BasicStatistics(String centerName) {
        super(centerName);
    }

    @Override
    void add(Index index, List<Double> list, double value) {
        list.add(value);
    }
}

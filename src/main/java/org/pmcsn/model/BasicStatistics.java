package org.pmcsn.model;

import java.util.ArrayList;
import java.util.List;

public class BasicStatistics extends AbstractStatistics {

    List<Double> probAccept;

    public BasicStatistics(String centerName) {
        super(centerName);
        probAccept = new ArrayList<>();
    }

    @Override
    void add(Index index, List<Double> list, double value) {
        list.add(value);
    }

    public List<Double> getProbAccept() {
        return probAccept;
    }

    public void addProbAccept(double probAccept) {
        this.probAccept.add(probAccept);
    }
}

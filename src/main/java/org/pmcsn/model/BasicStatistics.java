package org.pmcsn.model;

import java.util.ArrayList;
import java.util.List;

public class BasicStatistics extends AbstractStatistics {

    List<Double> probAccept;
    List<Double> jobServed;
    private final List<Double> busyTimeList = new ArrayList<>();

    public BasicStatistics(String centerName) {
        super(centerName);
        probAccept = new ArrayList<>();
        jobServed = new ArrayList<>();
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

    public List<Double> getJobServed() {
        return jobServed;
    }

    public void addJobServed(double value) {
        jobServed.add(value);
    }

    public void addBusyTime(double value) {
        busyTimeList.add(value);
    }

    public double getMeanBusyTime() {
        return busyTimeList.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
    }
}

package org.pmcsn.model;

import java.util.List;

// Helper class to hold metric name, values, and ACF value
public class BatchMetric {
    public String name;
    public List<Double> values;
    public double acfValue;

    public BatchMetric(String name, List<Double> values) {
        this.name = name;
        this.values = values;
    }

    public void setAcfValue(double acfValue) {
        this.acfValue = acfValue;
    }
}

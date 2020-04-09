package metrics;

import java.util.Map;

public interface Metric {
    void setup();
    double calculate();
    double calculateCluster(Map<String, Integer> clusters);
}

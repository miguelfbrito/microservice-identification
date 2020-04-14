package metrics;

import java.util.Map;

public interface Metric {
    double calculate();
    double calculateCluster(Map<String, Integer> clusters);
}

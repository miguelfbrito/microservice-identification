package metrics;

public interface Metric {
    void setup();
    double calculate();
}

package metrics;

import org.junit.jupiter.api.Test;
import projects.MetricsCalculator;
import projects.ProjectMetrics;

import java.util.List;

public class MetricsCalculatorTest {

    @Test
    public void calculateMetrics() {
        MetricsCalculator metricsCalculator = new MetricsCalculator();
        List<ProjectMetrics> calculator = metricsCalculator.calculate();

    }

}

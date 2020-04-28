package metrics;

import org.junit.jupiter.api.Test;
import projects.GenerateMetrics;
import projects.ProjectMetrics;

import java.util.List;

public class GenerateMetricsTest {

    @Test
    public void generateMetrics() {
        GenerateMetrics generateMetrics = new GenerateMetrics();
        List<ProjectMetrics> generate = generateMetrics.generate("chm & chd divided by counted services");

    }

}

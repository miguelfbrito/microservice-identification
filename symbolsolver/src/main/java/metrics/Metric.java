package metrics;

import java.io.IOException;
import java.util.Map;

public interface Metric {
    double calculateService() throws IOException;
}

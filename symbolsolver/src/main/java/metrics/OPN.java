package metrics;

import extraction.ExtractOperations;
import graph.entities.Service;
import parser.ParseResultServices;

/**
 * Coupling Metric
 * Calculates total method calls between classes
 */
public class OPN implements Metric {

    private ParseResultServices parseResultServices;

    public OPN(ParseResultServices parseResultServices) {
        this.parseResultServices = parseResultServices;
    }

    @Override
    public double calculateService() {
        ExtractOperations.extractAtServiceLevel(parseResultServices);
        ExtractOperations.extractAllClassOperationsToServiceLevel(parseResultServices.getServices());

        int totalOPN = 0;
        for (Service service : parseResultServices.getServices().values()) {
            totalOPN += service.getOperations().size();
        }

        return totalOPN;
    }
}

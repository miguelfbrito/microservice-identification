package metrics;

import extraction.ExtractOperations;
import graph.entities.Service;
import parser.ParseResultServices;

import java.util.HashSet;
import java.util.List;

/**
 * Coupling Metric
 * Calculates total method calls between classes
 */
public class OPN implements Metric {

    private ParseResultServices parseResultServices;
    private List<String> interfaces;

    public OPN(ParseResultServices parseResultServices, List<String> interfaces) {
        this.parseResultServices = parseResultServices;
        this.interfaces = interfaces;
    }

    @Override
    public double calculateService() {
        ExtractOperations.extractAtServiceLevelInterfaces(parseResultServices, new HashSet<>(interfaces));
        ExtractOperations.mapServices(parseResultServices.getServices());

        int totalOPN = 0;
        for (Service service : parseResultServices.getServices().values()) {
            totalOPN += service.getOperations().size();
        }

        return totalOPN;
    }
}

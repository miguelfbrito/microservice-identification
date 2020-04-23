package metrics;

import extraction.ExtractOperations;
import graph.DependencyEdge;
import graph.MyGraph;
import graph.entities.MyClass;
import graph.entities.Service;
import org.jgrapht.Graph;
import parser.ParseResult;

import java.util.Set;

/**
 * Coupling Metric
 * Calculates total method calls between classes
 */
public class OPN implements Metric {

    private ParseResult parseResult;

    public OPN(ParseResult parseResult) {
        this.parseResult = parseResult;
    }

    @Override
    public double calculateService() {
        ExtractOperations.extractAtServiceLevel(parseResult);
        calculateOPNEachService();

        int totalOPN = 0;
        for (Service service : parseResult.getServices().values()) {
            totalOPN += service.getOperations();
        }

        return totalOPN;
    }

    private void calculateOPNEachService() {
        for (Service service : parseResult.getServices().values()) {
            int totalOperationsPerService = 0;
            for (MyClass source : service.getClasses().values()) {
                totalOperationsPerService += source.getOperations().size();
            }
            service.setOperations(totalOperationsPerService);
        }
    }
}

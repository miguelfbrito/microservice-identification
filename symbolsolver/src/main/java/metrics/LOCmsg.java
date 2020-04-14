package metrics;

import graph.DependencyEdge;
import graph.entities.MyClass;
import graph.MyGraph;
import org.jgrapht.Graph;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Calculate the Lack of cohesion at message level.
 * The message level refers to names and datatypes of parameters on method declarations and its return datatypes.
 */
public class LOCmsg implements Metric {

    private MyGraph myGraph;

    public LOCmsg(MyGraph myGraph) {
        this.myGraph = myGraph;
    }

    @Override
    public double calculate() {
        Graph<MyClass, DependencyEdge> graph = this.myGraph.getGraph();

        int pairAmount = 0;
        double totalSimilarity = 0;

        for (DependencyEdge edge : graph.edgeSet()) {
            MyClass source = graph.getEdgeSource(edge);
            MyClass target = graph.getEdgeTarget(edge);

            totalSimilarity += calculateJaccardCoefficient(source, target);
            pairAmount++;
        }

        return (1 - totalSimilarity) / pairAmount;
    }

    private double calculateJaccardCoefficient(MyClass source, MyClass target) {
        Set<String> sourceDataTypes = new HashSet<>(source.getAllMethodsDataTypes());
        Set<String> targetDataTypes = new HashSet<>(target.getAllMethodsDataTypes());

        Set<String> union = new HashSet<>(sourceDataTypes);
        Set<String> intersection = new HashSet<>(sourceDataTypes);

        union.addAll(targetDataTypes);
        intersection.retainAll(targetDataTypes);

        return (double) intersection.size() / union.size();
    }

    @Override
    public double calculateCluster(Map<String, Integer> clusters) {
        return 0;
    }
}

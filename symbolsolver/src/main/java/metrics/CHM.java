package metrics;

import graph.DependencyEdge;
import graph.entities.MyClass;
import graph.MyGraph;
import graph.entities.MyMethod;
import org.jgrapht.Graph;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Calculate the CoHesion at Message level (CHM)
 * CHM = 1 - (Lack of Cohesion at message level)
 * The message level refers to datatypes of parameters on method declarations and its return datatypes.
 */
public class CHM implements Metric {

    private MyGraph myGraph;

    public CHM(MyGraph myGraph) {
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
        // Calculate jaccard coefficient for parameters data types
        Set<String> sourceParameters = new HashSet<>();
        Set<String> targetParameters = new HashSet<>();

        source.getMethods().forEach(m -> sourceParameters.addAll(m.getParametersDataType()));
        target.getMethods().forEach(m -> targetParameters.addAll(m.getParametersDataType()));
        double coefficientParameters = coefficient(sourceParameters, targetParameters);

        // Calculate jaccard coefficient for return data types
        Set<String> sourceReturn = new HashSet<>();
        Set<String> targetReturn = new HashSet<>();

        source.getMethods().forEach(m -> sourceReturn.add(m.getReturnDataType()));
        target.getMethods().forEach(m -> targetReturn.add(m.getReturnDataType()));
        double coefficientReturn = coefficient(sourceReturn, targetReturn);

        return (coefficientParameters + coefficientReturn) / 2;
    }

    private double coefficient(Set<String> source, Set<String> target) {
        Set<String> union = new HashSet<>(source);
        Set<String> intersection = new HashSet<>(source);

        union.addAll(target);
        intersection.retainAll(target);

        return (double) intersection.size() / union.size();
    }

    @Override
    public double calculateCluster(Map<String, Integer> clusters) {
        Graph<MyClass, DependencyEdge> graph = this.myGraph.getGraph();
        Map<Integer, ClusterLOCInfo> clusterResults = new HashMap<>();

        for (DependencyEdge edge : graph.edgeSet()) {
            MyClass source = graph.getEdgeSource(edge);
            MyClass target = graph.getEdgeTarget(edge);

            if (clusters.get(source.getQualifiedName()) != null &&
                    clusters.get(source.getQualifiedName()) == clusters.get(target.getQualifiedName())) {
                int clusterId = clusters.get(source.getQualifiedName());

                double jaccard = (1 - calculateJaccardCoefficient(source, target));

                if (clusterResults.containsKey(clusterId)) {
                    clusterResults.get(clusterId).pairAmount++;
                    clusterResults.get(clusterId).totalSimilarity += jaccard;
                } else {
                    clusterResults.put(clusterId, new ClusterLOCInfo(1, jaccard));
                }
            }
        }

        double totalDiff = 0;
        for (ClusterLOCInfo cluster : clusterResults.values()) {
            totalDiff += cluster.totalSimilarity / cluster.pairAmount;
        }

        return totalDiff / (double) clusterResults.size();
    }

}


class ClusterLOCInfo {
    public int pairAmount = 0;
    public double totalSimilarity = 0.0;

    public ClusterLOCInfo(int pairAmount, double totalSimilarity) {
        this.pairAmount = pairAmount;
        this.totalSimilarity = totalSimilarity;
    }

    @Override
    public String toString() {
        return "ClusterLOCInfo{" +
                "pairAmount=" + pairAmount +
                ", totalSimilarity=" + totalSimilarity +
                '}';
    }
}


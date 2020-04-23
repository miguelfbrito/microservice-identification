package metrics;

import graph.DependencyEdge;
import graph.MyGraph;
import graph.entities.MyClass;
import org.jgrapht.Graph;
import utils.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Calculate the CoHesion at Message level (CHM)
 * CHM = 1 - (Lack of Cohesion at message level)
 * The message level refers to datatypes of parameters on method declarations and its return datatypes.
 */
public class CHD implements Metric {

    private MyGraph myGraph;

    public CHD(MyGraph myGraph) {
        this.myGraph = myGraph;
    }

    @Override
    public double calculate() {
        return 0.0;
    }

    private double calculateJaccardCoefficient(MyClass source, MyClass target) {
        Set<String> sourceParameters = new HashSet<>();
        Set<String> targetParameters = new HashSet<>();

        source.getMethods().forEach(m -> sourceParameters.addAll(StringUtils.extractCamelCaseLower(m.getName())));
        target.getMethods().forEach(m -> targetParameters.addAll(StringUtils.extractCamelCaseLower(m.getName())));

        return JaccardCoefficient.calculate(sourceParameters, targetParameters);
    }


    @Override
    public double calculateCluster() {
        // TODO : atualizar para receber um ParseResult
        Map<String, Integer> clusters = new HashMap<>();
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



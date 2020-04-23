package metrics;

import graph.DependencyEdge;
import graph.entities.MyClass;
import graph.MyGraph;
import org.jgrapht.Graph;
import parser.ParseResult;

import java.util.*;

/**
 * Calculate the CoHesion at Message level (CHM)
 * CHM = 1 - (Lack of Cohesion at message level)
 * Calculates the similarity between datatypes of method inputs and outputs between classes
 */
public class CHM implements Metric {

    private MyGraph myGraph;
    private ParseResult parseResult;

    public CHM(MyGraph myGraph, ParseResult parseResult) {
        this.myGraph = myGraph;
        this.parseResult = parseResult;
    }


    private double calculateJaccardCoefficient(MyClass source, MyClass target) {
        // Calculate jaccard coefficient for parameters data types
        Set<String> sourceParameters = new HashSet<>();
        Set<String> targetParameters = new HashSet<>();

        source.getMethods().forEach(m -> sourceParameters.addAll(m.getParametersDataType()));
        target.getMethods().forEach(m -> targetParameters.addAll(m.getParametersDataType()));
        double coefficientParameters = JaccardCoefficient.calculate(sourceParameters, targetParameters);

        // Calculate jaccard coefficient for return data types
        Set<String> sourceReturn = new HashSet<>();
        Set<String> targetReturn = new HashSet<>();

        source.getMethods().forEach(m -> sourceReturn.addAll(m.getReturnDataType()));
        target.getMethods().forEach(m -> targetReturn.addAll(m.getReturnDataType()));
        double coefficientReturn = JaccardCoefficient.calculate(sourceReturn, targetReturn);

        return (coefficientParameters + coefficientReturn) / 2;
    }

    public Map<Integer, Integer> totalOperationsPerCluster(Map<String, Integer> clusters) {
        Map<String, MyClass> classes = parseResult.getClasses();
        Map<Integer, Integer> clusterIdMethodSum = new HashMap<>();

        for (Map.Entry<String, Integer> entry : clusters.entrySet()) {

            if (!classes.containsKey(entry.getKey())) {
                continue;
            }

            int totalMethods = classes.get(entry.getKey()).getMethods().size();

            if (!clusterIdMethodSum.containsKey(entry.getValue())) {
                clusterIdMethodSum.put(entry.getValue(), totalMethods);
            } else {
                clusterIdMethodSum.put(entry.getValue(), clusterIdMethodSum.get(entry.getValue()) + totalMethods);
            }
        }

        return clusterIdMethodSum;
    }

    @Override
    public double calculateService() {
        // TODO: atualizar para receber um ParseResult
        Map<String, Integer> clusters = new HashMap<>();

        Graph<MyClass, DependencyEdge> graph = this.myGraph.getGraph();
        Map<Integer, ClusterLOCInfo> clusterResults = new HashMap<>();


        Map<Integer, Integer> totalOperationsPerCluster = totalOperationsPerCluster(clusters);
        System.out.println("Number of clusters: " + totalOperationsPerCluster.size());

        /**
         *
         TODO: avaliar se o cálculo deve ser feito apartir dos pares? Não estamos a ignorar assim as classes que não têm
         métodos e portanto um jaccard de 1?
         */
        for (DependencyEdge edge : graph.edgeSet()) {
            MyClass source = graph.getEdgeSource(edge);
            MyClass target = graph.getEdgeTarget(edge);

            // Belong to the same cluster
            if (clusters.get(source.getQualifiedName()) != null &&
                    clusters.get(source.getQualifiedName()).equals(clusters.get(target.getQualifiedName()))) {
                int clusterId = clusters.get(source.getQualifiedName());

                double jaccard = (calculateJaccardCoefficient(source, target));

                if(totalOperationsPerCluster.get(clusterId) == 0){
                    System.out.println("Este cluster tem 0 operações!");
                }


/*
                int currTotal = totalOperationsPerCluster.get(clusterId);
                if (currTotal == 0) {
                    jaccard = 1;
                }
*/
/*
                // TODO : Reconsiderar se esta parte é necessária à equação
                else {
                    jaccard = jaccard / ((double) currTotal * (currTotal - 1) / 2);
                }
*/

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
            totalDiff += (cluster.totalSimilarity / cluster.pairAmount);
        }

        return clusterResults.isEmpty() ? 1 : totalDiff / (double) clusterResults.size();
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


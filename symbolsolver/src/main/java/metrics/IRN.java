package metrics;

import graph.DependencyEdge;
import graph.entities.MyClass;
import graph.MyGraph;
import graph.entities.Service;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultUndirectedGraph;
import parser.ParseResultServices;

import java.util.HashMap;
import java.util.Map;

/**
 * Coupling Metric
 * Calculates total method calls between classes
 */
public class IRN implements Metric {

    private MyGraph myGraph;
    private ParseResultServices parseResultServices;

    public IRN(MyGraph myGraph, ParseResultServices parseResultServices) {
        this.myGraph = myGraph;
        this.parseResultServices = parseResultServices;
    }

    @Override
    public double calculateService() {

        // For each edge, check if the source and target belong to the same cluster or not
        Graph<MyClass, DependencyEdge> graph = this.myGraph.getGraph();

        System.out.println("\nGraph total nodes: " + graph.vertexSet().size());
        System.out.println("Graph total edges: " + graph.edgeSet().size());
        System.out.println("Total services size: " + parseResultServices.getServices().size());
        System.out.println("Total classes size: " + parseResultServices.getClasses().size());

        double totalIrn = 0;

        Graph<Service, DependencyEdge> serviceGraph = new DefaultUndirectedGraph<>(DependencyEdge.class);

        for (DependencyEdge edge : graph.edgeSet()) {
            MyClass source = graph.getEdgeSource(edge);
            MyClass target = graph.getEdgeTarget(edge);

            // TODO: Handle null pointer
            Service serviceOfSource = parseResultServices.getClasses().get(source.getQualifiedName()).getService();
            Service serviceOfTarget = parseResultServices.getClasses().get(target.getQualifiedName()).getService();


            if (serviceOfSource != null && serviceOfTarget != null &&
                    serviceOfSource.getId() != serviceOfTarget.getId()) {
                System.out.println("Call to other service from " + source.getQualifiedName() + " -> " + target.getQualifiedName() + " -> " + edge.getValue());
                totalIrn += edge.getValue();

                if (!serviceGraph.containsVertex(serviceOfSource)) {
                    serviceGraph.addVertex(serviceOfSource);
                }

                if (!serviceGraph.containsVertex(serviceOfTarget)) {
                    serviceGraph.addVertex(serviceOfTarget);
                }

                DependencyEdge serviceEdge = serviceGraph.getEdge(serviceOfSource, serviceOfTarget);
                if (serviceEdge != null) {
                    serviceEdge.setValue(serviceEdge.getValue() + edge.getValue());
                } else {
                    serviceGraph.addEdge(serviceOfSource, serviceOfTarget, new DependencyEdge("label", edge.getValue()));
                }


            }

        }

        // TODO: Refactor
        int min = Integer.MAX_VALUE;
        int max = -1;
        for(DependencyEdge e : serviceGraph.edgeSet()){
            min = (int) Math.min(min, e.getValue());
            max = (int) Math.max(max, e.getValue());
        }
        int total = 0;
        for(DependencyEdge e : serviceGraph.edgeSet()){

            total += e.getValue();
        }

        System.out.println("Min: " + min);
        System.out.println("Max: " + max);
        System.out.println("Total: " + total);

        return totalIrn;
    }

    public MyGraph getMyGraph() {
        return myGraph;
    }
}

package metrics;

import graph.DependencyEdge;
import graph.entities.MyClass;
import graph.MyGraph;
import graph.entities.Service;
import org.jgrapht.Graph;
import parser.ParseResultServices;

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
        System.out.println("Total services size: "  + parseResultServices.getServices().size());
        System.out.println("Total classes size: "  + parseResultServices.getClasses().size());

        double totalIrn = 0;

        for (DependencyEdge edge : graph.edgeSet()) {
            MyClass source = graph.getEdgeSource(edge);
            MyClass target = graph.getEdgeTarget(edge);

            // TODO: Handle null pointer
            Service serviceOfSource = parseResultServices.getClasses().get(source.getQualifiedName()).getService();
            Service serviceOfTarget = parseResultServices.getClasses().get(target.getQualifiedName()).getService();

            if (serviceOfSource != null && serviceOfTarget != null &&
                    serviceOfSource.getId() != serviceOfTarget.getId()) {
                totalIrn += edge.getValue();
            }

        }
        return totalIrn;
    }

    public MyGraph getMyGraph() {
        return myGraph;
    }
}

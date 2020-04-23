package metrics;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.types.ResolvedType;
import graph.DependencyEdge;
import graph.entities.MyClass;
import graph.MyGraph;
import graph.entities.Service;
import org.jgrapht.Graph;
import parser.ParseResult;

import java.util.Map;
import java.util.Set;

/**
 * Coupling Metric
 * Calculates total method calls between classes
 */
public class IRN implements Metric {

    private MyGraph myGraph;
    private ParseResult parseResult;

    public IRN(MyGraph myGraph, ParseResult parseResult) {
        this.myGraph = myGraph;
        this.parseResult = parseResult;
    }


    @Override
    public double calculate() {
        Graph<MyClass, DependencyEdge> graph = this.myGraph.getGraph();

        Set<DependencyEdge> dependencyEdges = graph.edgeSet();
        double totalIRN = 0;
        for (DependencyEdge edge : dependencyEdges) {
            totalIRN += edge.getValue();
        }

        return totalIRN;
    }

    @Override
    public double calculateCluster() {

        // For each edge, check if the source and target belong to the same cluster or not
        Graph<MyClass, DependencyEdge> graph = this.myGraph.getGraph();

        System.out.println("\nGraph total nodes: " + graph.vertexSet().size());
        System.out.println("Graph total edges: " + graph.edgeSet().size());
        System.out.println("Total services size: "  + parseResult.getServices().size());
        System.out.println("Total classes size: "  + parseResult.getClasses().size());

        double totalIrn = 0;

        for (DependencyEdge edge : graph.edgeSet()) {
            MyClass source = graph.getEdgeSource(edge);
            MyClass target = graph.getEdgeTarget(edge);

            // TODO: Handle null pointer
            Service serviceOfSource = parseResult.getClasses().get(source.getQualifiedName()).getService();
            Service serviceOfTarget = parseResult.getClasses().get(target.getQualifiedName()).getService();

            if (serviceOfSource != null && serviceOfTarget != null &&
                    serviceOfSource.getClusterId() != serviceOfTarget.getClusterId()) {
                totalIrn += edge.getValue();
            }

        }
        return totalIrn;
    }

    public MyGraph getMyGraph() {
        return myGraph;
    }
}

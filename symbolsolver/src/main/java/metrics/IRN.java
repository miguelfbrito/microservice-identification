package metrics;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.types.ResolvedType;
import graph.DependencyEdge;
import graph.entities.MyClass;
import graph.MyGraph;
import org.jgrapht.Graph;

import java.util.Map;
import java.util.Set;

/**
 * Coupling Metric
 * Calculates total method calls between classes
 */
public class IRN implements Metric {

    private MyGraph myGraph;

    public IRN(MyGraph myGraph) {
        this.myGraph = myGraph;
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
    public double calculateCluster(Map<String, Integer> clusters) {

        // For each edge, check if the source and target belong to the same cluster or not
        Graph<MyClass, DependencyEdge> graph = this.myGraph.getGraph();

        double totalIrn = 0;
        for (DependencyEdge edge : graph.edgeSet()) {
            MyClass source = graph.getEdgeSource(edge);
            MyClass target = graph.getEdgeTarget(edge);

            if(!clusters.containsKey(source.getQualifiedName())){
                // System.out.println("\t[Node not found source] " + source.getSimpleName());
                continue;
            }

            if(!clusters.containsKey(target.getQualifiedName())) {
                // System.out.println("\t[Node not found target] " +target.getSimpleName());
                continue;
            }

            try {
                if (!clusters.get(source.getQualifiedName()).equals(clusters.get(target.getQualifiedName()))) {
                    totalIrn += edge.getValue();
                }
            } catch (NullPointerException e) {
                // TODO : Investigate why are there some occasions throwing a NullPointer
                // Probably because of a mismatch between identified classes from JavaParser and JavaLang
                // e.printStackTrace();
            }
        }
        return totalIrn;
    }

    public MyGraph getMyGraph() {
        return myGraph;
    }
}

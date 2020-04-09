package metrics;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import graph.DependencyEdge;
import graph.MyClass;
import graph.MyGraph;
import org.jgrapht.Graph;

import java.util.Map;
import java.util.Set;

/**
 * Coupling Metric
 * Calculates total method calls between classes
 */
public class IRNMetric implements Metric {

    private MyGraph myGraph;

    public IRNMetric(MyGraph myGraph) {
        this.myGraph = myGraph;
    }

    /**
     * Adds the dependencies to the graph, based on the method calls between classes.
     */
    @Override
    public void setup() {

        Graph<MyClass, DependencyEdge> graph = this.myGraph.getGraph();
        Set<MyClass> myClasses = graph.vertexSet();
        for (MyClass source : myClasses) {
            for (MethodCallExpr methodCall : source.getVisitor().findAll(MethodCallExpr.class)) {
                methodCall.getScope().ifPresent(rs -> {
                    try {
                        ResolvedType resolvedType = rs.calculateResolvedType();
                        String referencedQualifiedName = resolvedType.asReferenceType().getQualifiedName();

                        MyClass target = new MyClass(referencedQualifiedName);

                        DependencyEdge edge = graph.getEdge(source, target);
                        if (edge == null) {
                            graph.addEdge(source, target, new DependencyEdge(""));
                        } else {
                            edge.setValue(edge.getValue() + 1);
                        }


                    } catch (Exception e) {
                        System.out.println("[UnsolvedSymbolException] on " + rs.toString());
                    }
                });
            }
        }

        Set<DependencyEdge> dependencyEdges = this.myGraph.getGraph().edgeSet();
        for (DependencyEdge de : dependencyEdges)
            System.out.println(de.toString());

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

            System.out.println(source.getQualifiedName() + " - " + target.getQualifiedName());
            try {
                if (!clusters.get(source.getQualifiedName()).equals(clusters.get(target.getQualifiedName()))) {
                    totalIrn++;
                }
            } catch (NullPointerException e) {
                // TODO : Investigate why are there some ocasions throwing a NullPointer
                // Probably because of a mismatch between identified classes from JavaParser and JavaLang
                e.printStackTrace();
            }
        }
        return totalIrn;
    }

    public MyGraph getMyGraph() {
        return myGraph;
    }
}

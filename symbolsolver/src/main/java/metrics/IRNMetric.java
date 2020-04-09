package metrics;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import graph.DependencyEdge;
import graph.MyClass;
import graph.MyGraph;
import org.jgrapht.Graph;

import java.util.Set;

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
                        if(edge == null){
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

        System.out.println("Edge Set: ");
        Set<DependencyEdge> dependencyEdges = this.myGraph.getGraph().edgeSet();
        for (DependencyEdge de : dependencyEdges)
            System.out.println(de.toString());


    }

    @Override
    public double calculate() {
        return 0;
    }

    public MyGraph getMyGraph() {
        return myGraph;
    }
}

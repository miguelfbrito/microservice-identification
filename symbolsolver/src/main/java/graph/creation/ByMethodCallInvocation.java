package graph.creation;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.types.ResolvedType;
import graph.DependencyEdge;
import graph.MyGraph;
import graph.entities.MyClass;
import parser.ParseResultServices;

import java.util.Map;

public class ByMethodCallInvocation extends MyGraph {

    private ParseResultServices parseResultServices;

    public ByMethodCallInvocation(ParseResultServices parseResultServices) {
        super(parseResultServices);
        this.parseResultServices = parseResultServices;
        this.addEdges();
    }

    /**
     * Adds edges based on the method calls between classes.
     */
    @Override
    public void addEdges() {
        Map<String, MyClass> classes = parseResultServices.getClasses();

        for (MyClass source : classes.values()) {
            for (MethodCallExpr methodCall : source.getVisitor().findAll(MethodCallExpr.class)) {
                methodCall.getScope().ifPresent(rs -> {
                    try {
                        ResolvedType resolvedType = rs.calculateResolvedType();
                        MyClass target = classes.get(resolvedType.asReferenceType().getQualifiedName());

                        DependencyEdge edge = getGraph().getEdge(source, target);
                        if (edge == null) {
                            getGraph().addEdge(source, target, new DependencyEdge(""));
                        } else {
                            edge.setValue(edge.getValue() + 1);
                        }

                    } catch (Exception e) {
                        // System.out.println("[UnsolvedSymbolException] on " + rs.toString());
                    }
                });
            }
        }

    }
}

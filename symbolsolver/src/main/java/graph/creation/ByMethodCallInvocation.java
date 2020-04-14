package graph.creation;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.types.ResolvedType;
import graph.DependencyEdge;
import graph.MyGraph;
import graph.entities.MyClass;

import java.util.List;

public class ByMethodCallInvocation extends MyGraph {

    public ByMethodCallInvocation(List<CompilationUnit> compilationUnits){
        super(compilationUnits);
        this.addEdges();
    }

    /**
     * Adds edges based on the method calls between classes.
     */
    @Override
    public void addEdges() {
        for (MyClass source : getClasses().values()) {
            for (MethodCallExpr methodCall : source.getVisitor().findAll(MethodCallExpr.class)) {
                methodCall.getScope().ifPresent(rs -> {
                    try {
                        ResolvedType resolvedType = rs.calculateResolvedType();
                        MyClass target = getClasses().get(resolvedType.asReferenceType().getQualifiedName());

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

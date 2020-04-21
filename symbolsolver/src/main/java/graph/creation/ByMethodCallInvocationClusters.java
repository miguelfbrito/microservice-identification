package graph.creation;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.resolution.MethodUsage;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.*;
import graph.DependencyEdge;
import graph.MyGraph;
import graph.entities.MyClass;

import javax.lang.model.type.ReferenceType;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("DuplicatedCode")
public class ByMethodCallInvocationClusters extends MyGraph {

    private Map<String, Integer> clusters;

    public ByMethodCallInvocationClusters(List<CompilationUnit> compilationUnits, Map<String, Integer> clusters) {
        super(compilationUnits);
        this.clusters = clusters;
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

//                        rs.getParentNode().ifPresent(parent -> System.out.println("Parent of " + methodCall.getName()+ " -> " + parent));

                        // When calling a method to an external service, add the method being called to the list of operations
                        if(!clusters.get(source.getQualifiedName()).equals(clusters.get(target.getQualifiedName()))){
                            target.getOperations().add(methodCall.getName().toString());
                        }

                        System.out.println(target.getSimpleName() + ": "  + target.getOperations());

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

package graph.creation;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.types.ResolvedType;
import graph.DependencyEdge;
import graph.entities.MyClass;
import graph.MyGraph;

import java.util.List;

/**
 * Creates a dependency graph based on all the possible reference types between classes.
 * Any class used by a class will result in a dependency (object instantiation, method calls, static references, etc.)
 */
public class ByReferenceType extends MyGraph {

    public ByReferenceType(List<CompilationUnit> compilationUnits) {
        super(compilationUnits);
        this.addEdges();
    }

    @Override
    public void addEdges() {

        for (MyClass source : getClasses().values()) {
            for (Expression expression : source.getVisitor().findAll(Expression.class)) {
                try {
                    ResolvedType resolvedType = expression.calculateResolvedType();
                    if (resolvedType.isReferenceType()) {
                        String qualifiedName = resolvedType.asReferenceType().getQualifiedName();
                        MyClass target = getClasses().get(qualifiedName);
                        DependencyEdge edge = getGraph().getEdge(source, target);
                        if (target != null && !source.getQualifiedName().equals(target.getQualifiedName())) {
                            System.out.println(source.getQualifiedName() + " - " + target.getQualifiedName());
                            if (edge == null) {
                                getGraph().addEdge(source, target, new DependencyEdge(""));
                            } else {
                                edge.setValue(edge.getValue() + 1);
                            }
                        }

                    }
                } catch (UnsolvedSymbolException e) {
                    /*
                    TODO: Add logger later
                    Only happens when trying to match to expressions not support by .calculateResolvedType()
                    and we don't care about those
                     */
                } catch (UnsupportedOperationException e) {
                    // TODO: Add logger later
                }
            }
        }
    }
}

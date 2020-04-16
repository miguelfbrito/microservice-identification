package graph.creation;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import graph.DependencyEdge;
import graph.entities.MyClass;
import graph.MyGraph;

import java.util.InvalidPropertiesFormatException;
import java.util.List;

/**
 * Creates a dependency graph based on all the possible reference types between classes.
 * Any class used by a class will result in a dependency (object instantiation, method calls, static references, etc.)
 */
public class ByClassOrInterfaceType extends MyGraph {

    public ByClassOrInterfaceType(List<CompilationUnit> compilationUnits) {
        super(compilationUnits);
        this.addEdges();
    }

    @Override
    public void addEdges() {

        for (MyClass source : getClasses().values()) {
            for (ClassOrInterfaceType classOrInterfaceType : source.getVisitor().findAll(ClassOrInterfaceType.class)) {
                /*
                TODO: Identificar o tipo de node adequado que faz a ligação para o exterior em vez de recorrer
                a algo tão genérico como o Expression que está a causar problemas em certos tipos.
                 */

                try {
                    String qualifiedName = classOrInterfaceType.resolve().getQualifiedName();
                    System.out.println("[Found qualified name]  " + qualifiedName);

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
                    System.out.println("TYPE: " + qualifiedName);
                } catch(UnsupportedOperationException e){
                    /*
                        TODO: Find why this happens and how to do a pre-check for ClassOrInterfaceType that can't be resolved
                     */
                } catch (UnsolvedSymbolException e) {
                    /*
                       TODO: Add logger
                       This will only happen with references that do not belong internally to the project.
                       Any explicitly declared ClassOrInterface will work fine, and that's what we're looking for.
                       e.g.: Any references to Spring framework classes that aren't explicitly declared on the project
                       should trigger this exception.
                     */
                    System.out.println("[UnsolvedSymbolException] " + classOrInterfaceType.getName());
                }

            }

        }
    }
}
package graph;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import visitors.ClassOrInterfaceDeclarationVisitor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MyGraph<T> {

    private Graph<T, DependencyEdge> graph;

    public MyGraph() {
        graph = new DirectedMultigraph<>(DependencyEdge.class);
    }

    public void create(List<CompilationUnit> compilationUnits) {
        createNodes(compilationUnits);
        createEdges(compilationUnits);

    }

    private void createNodes(List<CompilationUnit> compilationUnits) {
        ClassOrInterfaceDeclarationVisitor classOrInterfaceDeclarationVisitor = new ClassOrInterfaceDeclarationVisitor();
        Set<T> nodes = new HashSet<>();

        for (CompilationUnit cu : compilationUnits) {
            cu.accept(classOrInterfaceDeclarationVisitor, nodes);

            for (T type : nodes) {
                graph.addVertex(type);
            }
            nodes.clear();
        }

        System.out.println("\nGRAPH");
        System.out.println(graph.toString());
    }

    private void createEdges(List<CompilationUnit> compilationUnits) {

        for (CompilationUnit compilationUnit : compilationUnits) {
            for (MethodCallExpr methodCall : compilationUnit.findAll(MethodCallExpr.class)) {
                methodCall.getScope().ifPresent(rs -> {
                    try {
                        ResolvedType resolvedType = rs.calculateResolvedType();
                        ResolvedReferenceType resolvedReferenceType = resolvedType.asReferenceType();
                        System.out.println("ResolvedType: " + resolvedType);
                        System.out.println("MethodCall " + methodCall);
                        System.out.println("Reference qualified name");
                        System.out.println(resolvedType.asReferenceType().getQualifiedName());

                    } catch (Exception e) {
                        System.out.println("[UnsolvedSymbolException] on " + rs.toString());
                    }
                });
            }
        }


    }

    public void addEdges() {

    }
}

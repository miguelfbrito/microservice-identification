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
import graph.entities.Service;

import javax.lang.model.type.ReferenceType;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("DuplicatedCode")
public class ByMethodCallInvocationClusters extends MyGraph {

    private Map<Integer, Service> clusters;

    public ByMethodCallInvocationClusters(List<CompilationUnit> compilationUnits, Map<Integer, Service> clusters) {
        super(compilationUnits, clusters);
        this.clusters = clusters;
        this.addEdges();
    }

    /**
     * Adds edges based on the method calls between classes.
     */

    private MyClass getTarget(String qualifiedName) {

        for (Map.Entry<Integer, Service> entryCluster : clusters.entrySet()) {
            if (entryCluster.getValue().getClasses().containsKey(qualifiedName)) {
                return entryCluster.getValue().getClasses().get(qualifiedName);
            }
        }

        return null;
    }


    // TODO : Changed this to not add edges, refactor later
    @Override
    public void addEdges() {

        for (Map.Entry<Integer, Service> entryCluster : clusters.entrySet()) {
            for (Map.Entry<String, MyClass> entryClasses : entryCluster.getValue().getClasses().entrySet()) {
                MyClass source = entryClasses.getValue();
                if(source.getVisitor() == null){
                    System.out.println();
                    continue;
                }
                for (MethodCallExpr methodCall : source.getVisitor().findAll(MethodCallExpr.class)) {
                    methodCall.getScope().ifPresent(rs -> {
                        try {
                            ResolvedType resolvedType = rs.calculateResolvedType();
                            String targetName = resolvedType.asReferenceType().getQualifiedName();
                            MyClass target = getTarget(targetName);

                            if (target != null) {
                                target.getOperations().add(methodCall.getName().toString());
                            }

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


            int totalOperations = 0;
            for (MyClass source : entryCluster.getValue().getClasses().values()) {
                System.out.println(source.getQualifiedName() + ": " + source.getOperations());
                totalOperations += source.getOperations().size();
            }

            entryCluster.getValue().setOperations(totalOperations);
            System.out.println("Total Operations per Service: " + totalOperations);

        }

        int totalOperationsServiceLevel = 0;
        for(Service service : clusters.values()){
            totalOperationsServiceLevel += service.getOperations();
        }

        System.out.println("Total Operations Service Level: " + totalOperationsServiceLevel);



    }
}

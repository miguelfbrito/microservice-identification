package utils;

import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.google.gson.annotations.Expose;
import graph.entities.MyClass;
import graph.entities.MyMethod;

import java.util.*;
import java.util.stream.Collectors;

public class MyClassDTO {
    @Expose
    private String qualifiedName;
    @Expose
    private List<String> annotations;
    @Expose
    private List<String> variables;
    @Expose
    private List<String> variablesDependencies;
    @Expose
    private Map<String, MyMethod> methods; // method name, method reference
    @Expose
    private Map<String, String> methodInvocations;
    @Expose
    private List<String> implementedTypes;
    @Expose
    private List<String> extendedTypes;

    public MyClassDTO(MyClass myClass) {
        this.qualifiedName = myClass.getQualifiedName();
        this.annotations = myClass.getAnnotations().stream().map(NodeWithName::getNameAsString).collect(Collectors.toList());
        this.variables = myClass.getVariables().stream().map(v -> v.getType().asString() + " " + v.getName().toString()).collect(Collectors.toList());
        this.variablesDependencies = handleVariablesDependencies(myClass.getVariables());
        this.methods = myClass.getMethods();
        this.methodInvocations = handleMethodInvocations(myClass.getMethodInvocations());
        this.implementedTypes = new ArrayList<>(myClass.getImplementedTypes());
        this.extendedTypes = new ArrayList<>(myClass.getExtendedTypes());
    }


    public Map<String, String> handleMethodInvocations(List<MethodCallExpr> methodInvocations) {
        Map<String, String> processedMethods = new HashMap<>();
        for (MethodCallExpr methodCallExpr : methodInvocations) {
            methodCallExpr.getScope().ifPresent(mc -> {
                try {
                    ResolvedReferenceType resolvedReferenceType = mc.calculateResolvedType().asReferenceType();
                    String targetClassName = resolvedReferenceType.getQualifiedName();
                    processedMethods.put(methodCallExpr.getNameAsString(), targetClassName); // TODO: handle method overloading causing conflicts and getting overwritten
                } catch (UnsolvedSymbolException e) {
                    // When it tries to resolve a class not explicitly present in the project. We don't care about those.
                } catch (UnsupportedOperationException e) {
                    //e.printStackTrace();
                } catch (RuntimeException e) {
                    // TODO : reevaluate
                }
            });
        }
        return processedMethods;
    }

    public List<String> handleVariablesDependencies(List<VariableDeclarator> variableDeclarators) {
        List<String> varDependencies = new ArrayList<>();
        for (VariableDeclarator variableDeclarator : variableDeclarators) {
            try {
                ResolvedType resolve = variableDeclarator.getType().resolve();
                resolve.asReferenceType().getQualifiedName();
                String typeClassName = variableDeclarator.getType().resolve().asReferenceType().getQualifiedName();
                varDependencies.add(typeClassName);
            } catch (UnsolvedSymbolException e) {
                // When it tries to resolve a class not explicitly present in the project. We don't care about those.
            } catch (UnsupportedOperationException e) {
                //e.printStackTrace();
            }
        }
        return varDependencies;
    }
}

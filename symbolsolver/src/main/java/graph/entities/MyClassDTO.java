package graph.entities;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.google.gson.annotations.Expose;

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
    private List<String> dependencies;
    @Expose
    private Map<String, MyMethod> methods; // method name, method reference
    @Expose
    private Map<String, String> methodInvocations;
    @Expose
    private List<String> implementedTypes;
    @Expose
    private List<String> extendedTypes;

    private Set<String> validClasses;

    public MyClassDTO(MyClass myClass, Set<String> validClasses) {
        this.validClasses = validClasses;
        this.qualifiedName = myClass.getQualifiedName();
        this.annotations = myClass.getAnnotations().stream().map(NodeWithName::getNameAsString).collect(Collectors.toList());
        this.variables = myClass.getVariables().stream().map(v -> v.getType().asString() + " " + v.getName().toString()).collect(Collectors.toList());
        this.methods = myClass.getMethods();
        this.methodInvocations = extractMethodInvocations(myClass.getMethodInvocations());
        this.implementedTypes = new ArrayList<>(myClass.getImplementedTypes());
        this.extendedTypes = new ArrayList<>(myClass.getExtendedTypes());
        if (myClass.getQualifiedName().equals("org.springframework.samples.petclinic.vet.Vet")) {
            System.out.println("HI");
        }
        this.dependencies = extractDependencies(myClass.getVariables());
    }


    public Map<String, String> extractMethodInvocations(List<MethodCallExpr> methodInvocations) {
        Map<String, String> processedMethods = new HashMap<>();
        int total = 0;

        for (MethodCallExpr methodCallExpr : methodInvocations) {
            Optional<Expression> scope = methodCallExpr.getScope();

            if (scope.isPresent()) {
                Expression expression = scope.get();
                try {
                    ResolvedReferenceType resolvedReferenceType = expression.calculateResolvedType().asReferenceType();
                    String targetClassName = resolvedReferenceType.getQualifiedName();
                    if (isValidClass(targetClassName)) {
                        processedMethods.put(methodCallExpr.getNameAsString(), targetClassName); // TODO: handle method overloading causing conflicts and getting overwritten
                        total++;
                    }

                } catch (UnsolvedSymbolException e) {
                    // When it tries to resolve a class not explicitly present in the project. We don't care about those.
                } catch (UnsupportedOperationException e) {
                    //e.printStackTrace();
                } catch (RuntimeException e) {
                    // TODO : reevaluate
                }

            }
        }
        return processedMethods;
    }

    public List<String> extractDependencies(List<VariableDeclarator> variableDeclarators) {
        List<String> dependencyList = new ArrayList<>();
        for (VariableDeclarator variableDeclarator : variableDeclarators) {
            try {
                // This is a bit hacky but currently there's no support on javaparser to get directly the typeArguments of a Type
                resolveTargetClassFromSubTypes(dependencyList, variableDeclarator.getType());
            } catch (UnsolvedSymbolException | UnsupportedOperationException e) {
                // When it tries to resolve a class not explicitly present in the project. We don't care about those.
                e.printStackTrace();
            }
        }

        for (MyMethod method : methods.values()) {
            MethodDeclaration methodDeclaration = method.getVisitor();
            resolveTargetClassFromSubTypes(dependencyList, methodDeclaration.getType());

            methodDeclaration.getParameters().forEach(parameter -> {
                List<ClassOrInterfaceType> referencesParametersType = parameter.getType().findAll(ClassOrInterfaceType.class);
                try {
                    for (ClassOrInterfaceType ref : referencesParametersType) {
                        String paramTargetClassName = ref.resolve().getQualifiedName();
                        if (isValidClass(paramTargetClassName))
                            dependencyList.add(paramTargetClassName);
                    }
                } catch (UnsolvedSymbolException | UnsupportedOperationException e) {

                }
            });
        }
        return dependencyList;
    }

    private void resolveTargetClassFromSubTypes(List<String> dependencyList, Type type) {
        List<ClassOrInterfaceType> referencesReturnType = type.findAll(ClassOrInterfaceType.class);
        for (ClassOrInterfaceType ref : referencesReturnType) {
            try {
                String targetClassName = ref.resolve().getQualifiedName();
                if(isValidClass(targetClassName))
                    dependencyList.add(targetClassName);
            } catch (UnsolvedSymbolException | UnsupportedOperationException e) {
            }
        }
    }

    public Map<String, String> getMethodInvocations() {
        return methodInvocations;
    }

    private boolean isValidClass(String qualifiedName) {
        return this.validClasses.contains(qualifiedName);
    }
}


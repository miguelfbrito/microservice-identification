package graph.creation;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.types.ResolvedType;
import graph.DependencyEdge;
import graph.MyGraph;
import graph.entities.MyClass;
import parser.ParseResultServices;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        Set<String> validClasses = new HashSet<String>(classes.values().stream().map(MyClass::getQualifiedName).collect(Collectors.toList()));

        for (MyClass source : classes.values()) {
            for (MethodCallExpr methodCall : source.getVisitor().findAll(MethodCallExpr.class)) {
                methodCall.getScope().ifPresent(rs -> {
                    try {
                        ResolvedType resolvedType = rs.calculateResolvedType();
                        MyClass target = classes.get(resolvedType.asReferenceType().getQualifiedName());

                        //if(isMethodCallGetterOrSetter(methodCall)) {
                        // return;
                        // }

                        DependencyEdge edge = getGraph().getEdge(source, target);
                        if (isValidClass(target.getQualifiedName(), validClasses)) {
                            if (edge == null) {
                                getGraph().addEdge(source, target, new DependencyEdge(""));
                            } else {
                                edge.setValue(edge.getValue() + 1);
                            }
                        }

                    } catch (Exception e) {
                        // System.out.println("[UnsolvedSymbolException] on " + rs.toString());
                    }
                });
            }
        }

    }

    private boolean isMethodCallGetterOrSetter(MethodCallExpr exp) {
        Matcher matchGet = Pattern.compile("^get").matcher(exp.getNameAsString());
        Matcher matchSet = Pattern.compile("^set").matcher(exp.getNameAsString());
        return (matchGet.find() && exp.getArguments().size() == 0) || (matchSet.find() && exp.getArguments().size() == 1);
    }

    private boolean isValidClass(String qualifiedName, Set<String> validClasses) {
        return validClasses.contains(qualifiedName);
    }
}

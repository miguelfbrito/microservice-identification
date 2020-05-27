package extraction;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import graph.entities.MyClass;
import graph.entities.Service;
import parser.ParseResultServices;
import utils.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class ExtractOperations {

    public static void extractAtServiceLevelInterfaces(ParseResultServices parseResultServices, Set<String> interfaces) {
        System.out.println("HELLO");
        Map<Integer, Service> services = parseResultServices.getServices();
        Map<String, MyClass> classes = parseResultServices.getClasses();


        for (Map.Entry<Integer, Service> service : services.entrySet()) {
            for (Map.Entry<String, MyClass> classe : service.getValue().getClasses().entrySet()) {
                // We found an interface, get the operations
                if (interfaces.contains(classe.getKey())) {
                    for (MethodDeclaration md : classe.getValue().getVisitor().findAll(MethodDeclaration.class)) {
                        String methodName = md.getNameAsString();
                        System.out.println("New OPN md: " + methodName);


                        if (!StringUtils.isMethodCallGetterOrSetter(methodName, md.getParameters().size())) {
                            classe.getValue().getOperations().add(methodName);
                        } else {
                            System.out.println("Filtered out " + md.getNameAsString());
                        }
                    }

                }

            }
        }

    }

    /**
     * Extracts all operations by traversing through every class for each service and identifying
     * the method invocations to other services
     *
     * @param parseResultServices
     */
    public static void extractAtServiceLevel(ParseResultServices parseResultServices) {
        Map<Integer, Service> services = parseResultServices.getServices();
        Map<String, MyClass> classes = parseResultServices.getClasses();

        for (Map.Entry<Integer, Service> service : services.entrySet()) {
            for (Map.Entry<String, MyClass> entryClasses : service.getValue().getClasses().entrySet()) {

                MyClass source = entryClasses.getValue();
                if (source.getVisitor() == null) {
                    System.out.println("[Source class not found]");
                    continue;
                }


                for (MethodCallExpr methodCallExpr : source.getVisitor().findAll(MethodCallExpr.class)) {
                    Optional<Expression> scope = methodCallExpr.getScope();

                    if (scope.isPresent()) {
                        Expression expression = scope.get();
                        try {
                            ResolvedReferenceType resolvedReferenceType = expression.calculateResolvedType().asReferenceType();
                            String methodName = methodCallExpr.getNameAsString();
                            String targetName = resolvedReferenceType.getQualifiedName();
                            MyClass target = classes.get(targetName);

                            if (source.getService() != null && target.getService() != null
                                    && source.getService().getId() != target.getService().getId()) {
                                //if (!StringUtils.isMethodCallGetterOrSetter(methodCallExpr)) {
                                target.getOperations().add(methodCallExpr.getNameAsString());
                                //}
                            }

                        } catch (UnsolvedSymbolException e) {
                            // When it tries to resolve a class not explicitly present in the project. We don't care about those.
                        } catch (UnsupportedOperationException e) {
                            //e.printStackTrace();
                        } catch (RuntimeException e) {
                            // TODO CRITICAL : reevaluate
                        }

                    }
                }


/*

                for (MethodCallExpr methodCall : source.getVisitor().findAll(MethodCallExpr.class)) {
                    methodCall.getScope().ifPresent(rs -> {
                        try {
                            ResolvedType resolvedType = rs.calculateResolvedType();
                            String targetName = resolvedType.asReferenceType().getQualifiedName();
                            MyClass target = classes.get(targetName);

                            // Target and source must exist in different services to be considered an operation
                            if (source.getService() != null && target.getService() != null
                                    && source.getService().getId() != target.getService().getId()) {
                                if (!StringUtils.isMethodCallGetterOrSetter(methodCall)) {
                                    target.getOperations().add(methodCall.getName().toString());
                                }
                            }
                        } catch (UnsolvedSymbolException e) {
                            // System.out.println("[UnsolvedSymbolException] on " + rs.toString());
                        }
                    });
                }
*/
            }
        }
    }

    public static void mapServices(Map<Integer, Service> services) {
        for (Service service : services.values()) {
            Map<String, String> operations = new HashMap<>();
            for (MyClass source : service.getClasses().values()) {
                for (String op : source.getOperations()) {
                    operations.put(op, source.getQualifiedName());
                }
            }
            service.setOperations(operations);
        }
    }
}




package extraction;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.types.ResolvedType;
import graph.entities.MyClass;
import graph.entities.Service;
import parser.ParseResult;
import java.util.Map;

public class ExtractOperations {

    public static void extractAtServiceLevel(ParseResult parseResult) {
        Map<Integer, Service> services = parseResult.getServices();
        Map<String, MyClass> classes = parseResult.getClasses();

        for (Map.Entry<Integer, Service> service : services.entrySet()) {
            for (Map.Entry<String, MyClass> entryClasses : service.getValue().getClasses().entrySet()) {

                MyClass source = entryClasses.getValue();
                if (source.getVisitor() == null) {
                    System.out.println("[Source class not found]");
                    continue;
                }

                for (MethodCallExpr methodCall : source.getVisitor().findAll(MethodCallExpr.class)) {
                    methodCall.getScope().ifPresent(rs -> {
                        try {
                            ResolvedType resolvedType = rs.calculateResolvedType();
                            String targetName = resolvedType.asReferenceType().getQualifiedName();
                            MyClass target = classes.get(targetName);

                            if (target != null) {
                                target.getOperations().add(methodCall.getName().toString());
                            }
                        } catch (Exception e) {
                            // System.out.println("[UnsolvedSymbolException] on " + rs.toString());
                        }
                    });
                }
            }


            int totalOperationsPerService = 0;
            for (MyClass source : service.getValue().getClasses().values()) {
                System.out.println(source.getQualifiedName() + ": " + source.getOperations());
                totalOperationsPerService += source.getOperations().size();
            }

            service.getValue().setOperations(totalOperationsPerService);
            System.out.println("OPN per Service: " + totalOperationsPerService);
        }

        int totalOperationsAllServices = 0;
        for (Service service : services.values()) {
            totalOperationsAllServices += service.getOperations();
        }

        System.out.println("OPN for all services:" + totalOperationsAllServices);
    }
}

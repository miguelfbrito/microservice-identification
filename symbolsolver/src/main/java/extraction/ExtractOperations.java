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

                            // Target and source must exist in different services to be considered an operation
                            if (target != null && source.getService().getId() != target.getService().getId()) {
                                target.getOperations().add(methodCall.getName().toString());
                            }
                        } catch (Exception e) {
                            // System.out.println("[UnsolvedSymbolException] on " + rs.toString());
                        }
                    });
                }
            }

        }
    }
}

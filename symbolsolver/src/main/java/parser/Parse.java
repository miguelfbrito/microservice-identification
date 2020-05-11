package parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import graph.entities.MyClass;
import graph.entities.MyMethod;
import graph.entities.Service;
import utils.FileUtils;
import graph.entities.MyClassDTO;
import utils.StringUtils;
import visitors.*;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parse {

    public ParseResultServices completeParseClusters(List<CompilationUnit> compilationUnits, String clusterString) {
        Map<String, MyClass> classes = extractClasses(compilationUnits);
        populateClassesWithMethodDeclarations(classes);
        Map<Integer, Service> services = populateServicesWithClasses(clusterString, classes);

        return new ParseResultServices(classes, services);
    }

    public void completeParse(List<CompilationUnit> compilationUnits) {
        System.out.println("Method invocations");
        Map<String, MyClass> classes = extractClasses(compilationUnits);
        System.out.println("Classes: " + classes.toString());
        populateClassesWithMethodDeclarations(classes);
        parseClasses(classes);
    }

    public Map<String, MyClass> extractClasses(List<CompilationUnit> compilationUnits) {
        Map<String, MyClass> classes = new HashMap<>();
        Set<ClassOrInterfaceDeclaration> nodes = new HashSet<>();

        for (CompilationUnit cu : compilationUnits) {
            cu.accept(new ClassOrInterfaceDeclarationVisitor(), nodes);
            for (ClassOrInterfaceDeclaration node : nodes) {
                node.getFullyQualifiedName().ifPresent(name -> {
                    // Ignore test files
                    Pattern p = Pattern.compile("[Tt]ests?");
                    Matcher m = p.matcher(name);
                    if (!m.find()) {
                        MyClass myClass = new MyClass(node);
                        classes.put(name, myClass);
                        System.out.println("Adding class of name " + name);

                        addExtendAndImplementTypes(myClass, node);
                    }
                });
            }
            nodes.clear();
        }
        return classes;
    }

    public void addExtendAndImplementTypes(MyClass myClass, ClassOrInterfaceDeclaration node) {
        for (ClassOrInterfaceType extendedTypes : node.getExtendedTypes()) {
            try {
                ResolvedReferenceType resolve = extendedTypes.resolve();
                if (resolve != null) {
                    myClass.getExtendedTypes().add(resolve.getQualifiedName());
                }
            } catch (UnsolvedSymbolException e) {
                // Happens when it tries to resolve a class not explicitly present in the project source code
                // We don't care about those, so, just ignore them
            }
        }
        for (ClassOrInterfaceType implementedTypes : node.getImplementedTypes()) {
            try {
                ResolvedReferenceType resolve = implementedTypes.resolve();
                if (resolve != null) {
                    myClass.getImplementedTypes().add(resolve.getQualifiedName());
                }
            } catch (UnsolvedSymbolException e) {
                // Happens when it tries to resolve a class not explicitly present in the project source code
                // We don't care about those, so, just ignore them
            }
        }
    }

    public Map<Integer, Service> populateServicesWithClasses(String string, Map<String, MyClass> myClasses) {

        Map<String, Integer> classesWithServiceId = StringUtils.readClustersFromString(string);
        Map<Integer, Service> clusters = new HashMap<>();

        // Adds each class to its own service id extracted from a String
        for (String className : classesWithServiceId.keySet()) {
            int clusterId = classesWithServiceId.get(className);
            Service service = clusters.get(clusterId);
            MyClass myClass = myClasses.get(className);

            if (myClass == null) {
                System.out.println("[Class Not Found] " + className);
                continue;
            }

            if (service == null) {
                service = new Service(clusterId);
                service.getClasses().put(className, myClasses.get(className));
                myClass.setService(service);
                clusters.put(service.getId(), service);
            } else {
                service.getClasses().put(className, myClasses.get(className));
                myClass.setService(service);
            }
        }

        return clusters;
    }

    public void populateClassesWithMethodDeclarations(Map<String, MyClass> myClasses) {
        // TODO : Consider refactoring this into a Visitor to be consistent with others
        for (MyClass myClass : myClasses.values()) {
            myClass.getVisitor().findAll(MethodDeclaration.class).forEach(methodDeclaration -> {
                        MyMethod method = new MyMethod(methodDeclaration.getName().toString());
                        List<String> parametersDataType = new ArrayList<>();

                        for (Parameter parameter : methodDeclaration.getParameters()) {
                            parametersDataType.addAll(StringUtils.extractVariableType(parameter.getTypeAsString()));
                        }

                        method.setParametersDataType(parametersDataType);
                        method.setReturnDataType(StringUtils.extractVariableType(methodDeclaration.getTypeAsString()));

                        myClass.getMethods().put(methodDeclaration.getName().toString(), method);
                        method.setMyClass(myClass);
                    }
            );
        }
    }

    public void parseClasses(Map<String, MyClass> myClasses) {
        // TODO : Method declarations are being identified elsewhere, consider refactoring it to a visitor and add it here.
        Set<MarkerAnnotationExpr> annotations = new HashSet<>();
        List<VariableDeclarator> variables = new ArrayList<>();
        List<MethodCallExpr> methodCallInvocations = new ArrayList<>();

        for (MyClass myClass : myClasses.values()) {
            ClassOrInterfaceDeclaration visitor = myClass.getVisitor();
            // visitor.accept(new FieldDeclarationVisitor(), fields);
            visitor.accept(new AnnotationVisitor(), annotations);
            visitor.accept(new VariableDeclaratorVisitor(), variables);
            visitor.accept(new MethodCallExprVisitor(), methodCallInvocations);
            System.out.println("\n\n\nClass " + myClass.getSimpleName());
            for (MethodCallExpr mc : methodCallInvocations) {
                System.out.println(mc.getName().toString());
            }

            myClass.setAnnotations(new ArrayList<>(annotations));
            myClass.setVariables(variables);
            myClass.setMethodInvocations(methodCallInvocations);
        }

        Map<String, MyClassDTO> myClassDTOS = new HashMap<>();
        try {
            for (MyClass myClass : myClasses.values()) {
                myClassDTOS.put(myClass.getQualifiedName(), new MyClassDTO(myClass));
            }
            FileUtils.jsonDump(myClassDTOS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}


// variables.name
// variables.type -> para dar o resolve
package parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import graph.entities.MyClass;
import graph.entities.MyMethod;
import graph.entities.Service;
import utils.StringUtils;
import visitors.ClassOrInterfaceDeclarationVisitor;

import java.util.*;

public class Parse {

    public ParseResult completeParseClusters(List<CompilationUnit> compilationUnits, String clusterString) {
        Map<String, MyClass> classes = extractClasses(compilationUnits);
        populateClassesWithMethodDeclarations(classes);
        Map<Integer, Service> services = populateServicesWithClasses(clusterString, classes);

        return new ParseResult(classes, services);
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
                clusters.put(service.getClusterId(), service);
            } else {
                service.getClasses().put(className, myClasses.get(className));
                myClass.setService(service);
            }
        }

        return clusters;
    }

    public Map<String, MyClass> extractClasses(List<CompilationUnit> compilationUnits) {

        Map<String, MyClass> classes = new HashMap<>();
        Set<ClassOrInterfaceDeclaration> nodes = new HashSet<>();

        for (CompilationUnit cu : compilationUnits) {
            cu.accept(new ClassOrInterfaceDeclarationVisitor(), nodes);

            for (ClassOrInterfaceDeclaration node : nodes) {
                MyClass myClass = new MyClass(node);
                node.getFullyQualifiedName().ifPresent(name -> classes.put(name, myClass));
            }
            nodes.clear();
        }

        return classes;
    }

    public void populateClassesWithMethodDeclarations(Map<String, MyClass> myClasses) {
        for (MyClass myClass : myClasses.values()) {
            myClass.getVisitor().findAll(MethodDeclaration.class).forEach(methodDeclaration -> {
                        MyMethod method = new MyMethod(methodDeclaration.getName().toString());
                        List<String> parametersDataType = new ArrayList<>();

                        for (Parameter parameter : methodDeclaration.getParameters()) {
                            parametersDataType.addAll(StringUtils.extractVariableType(parameter.getTypeAsString()));
                        }

                        method.setParametersDataType(parametersDataType);
                        method.setReturnDataType(StringUtils.extractVariableType(methodDeclaration.getTypeAsString()));

                        myClass.getMethods().add(method);
                    }
            );
        }
    }
}




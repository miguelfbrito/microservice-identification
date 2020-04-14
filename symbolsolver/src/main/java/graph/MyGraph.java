package graph;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import graph.entities.MyClass;
import graph.entities.MyMethod;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedMultigraph;
import visitors.ClassOrInterfaceDeclarationVisitor;

import java.lang.reflect.Method;
import java.util.*;

public class MyGraph {

    private Graph<MyClass, DependencyEdge> graph;
    private Map<String, MyClass> classes;

    public MyGraph() {
        graph = new DirectedMultigraph<>(DependencyEdge.class);
        classes = new HashMap<>();
    }

    public MyGraph(List<CompilationUnit> compilationUnits) {
        graph = new DirectedMultigraph<>(DependencyEdge.class);
        classes = new HashMap<>();
        this.addNodes(compilationUnits);
        this.extractMethodDeclarations();
    }

    public void create(List<CompilationUnit> compilationUnits) {
        addNodes(compilationUnits);
        //createEdges(compilationUnits);
    }

    public void addNodes(List<CompilationUnit> compilationUnits) {
        ClassOrInterfaceDeclarationVisitor classOrInterfaceDeclarationVisitor = new ClassOrInterfaceDeclarationVisitor();
        Set<ClassOrInterfaceDeclaration> nodes = new HashSet<>();

        for (CompilationUnit cu : compilationUnits) {
            cu.accept(classOrInterfaceDeclarationVisitor, nodes);

            for (ClassOrInterfaceDeclaration node : nodes) {
                MyClass myClass = new MyClass(node);
                node.getFullyQualifiedName().ifPresent(name -> classes.put(name, myClass));
                graph.addVertex(myClass);
            }
            nodes.clear();
        }

        System.out.println("\nGraph:");
        System.out.println(graph.toString());
    }

    public void addEdges(){

    }

    public void extractMethodDeclarations() {
        for (MyClass myClass : this.graph.vertexSet()) {
            List<MyMethod> methods = new ArrayList<>();
            myClass.getVisitor().findAll(MethodDeclaration.class).forEach(methodDeclaration -> {
                        MyMethod method = new MyMethod(methodDeclaration.getName().toString());
                        List<String> parametersDataType = new ArrayList<>();

                        for (Parameter parameter : methodDeclaration.getParameters()) {
                            parametersDataType.add(parameter.getTypeAsString());
                        }

                        method.setParametersDataType(parametersDataType);
                        method.setReturnDataType(methodDeclaration.getTypeAsString());
                        methods.add(method);
                    }
            );
            myClass.setMethods(methods);
        }
    }


    public Graph<MyClass, DependencyEdge> getGraph() {
        return graph;
    }

    public Map<String, MyClass> getClasses() {
        return classes;
    }

    public void setGraph(Graph<MyClass, DependencyEdge> graph) {
        this.graph = graph;
    }

    public void setClasses(Map<String, MyClass> classes) {
        this.classes = classes;
    }
}

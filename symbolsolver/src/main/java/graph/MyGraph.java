package graph;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import graph.entities.MyClass;
import graph.entities.MyMethod;
import graph.entities.Service;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedMultigraph;
import parser.ParseResult;
import utils.StringUtils;
import visitors.ClassOrInterfaceDeclarationVisitor;

import java.util.*;

public class MyGraph {

    private Graph<MyClass, DependencyEdge> graph;

    public MyGraph() {
        graph = new DirectedMultigraph<>(DependencyEdge.class);
    }

    public MyGraph(ParseResult parseResult) {
        graph = new DirectedMultigraph<>(DependencyEdge.class);
        this.addNodes(parseResult);
    }

    public void addNodes(ParseResult parseResult) {
        Map<String, MyClass> classes = parseResult.getClasses();

        for(MyClass myClass : classes.values()){
            graph.addVertex(myClass);
        }

        System.out.println("\nGraph:");
        System.out.println(graph.toString());
    }

    public void addEdges() {
    }

    public Graph<MyClass, DependencyEdge> getGraph() {
        return graph;
    }

    public void setGraph(Graph<MyClass, DependencyEdge> graph) {
        this.graph = graph;
    }



}

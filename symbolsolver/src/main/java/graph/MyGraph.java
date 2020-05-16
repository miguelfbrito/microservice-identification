package graph;

import graph.entities.MyClass;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedMultigraph;
import parser.ParseResultServices;

import java.util.*;

public class MyGraph {

    private Graph<MyClass, DependencyEdge> graph;

    public MyGraph() {
        graph = new DirectedMultigraph<>(DependencyEdge.class);
    }

    public MyGraph(ParseResultServices parseResultServices) {
        graph = new DirectedMultigraph<>(DependencyEdge.class);
        this.addNodes(parseResultServices);
    }

    public void addNodes(ParseResultServices parseResultServices) {
        Map<String, MyClass> classes = parseResultServices.getClasses();

        for(MyClass myClass : classes.values()){
            graph.addVertex(myClass);
        }
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

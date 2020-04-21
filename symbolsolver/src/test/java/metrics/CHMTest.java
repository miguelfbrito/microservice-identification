package metrics;

import com.github.javaparser.ast.CompilationUnit;
import graph.DependencyEdge;
import graph.MyGraph;
import graph.creation.ByClassOrInterfaceType;
import graph.entities.MyClass;
import graph.entities.MyMethod;
import metrics.CHM;
import metrics.Metric;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import parser.Parser;
import utils.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Disabled.*;

public class CHMTest {




    public MyClass createClassOnGraph(MyGraph myGraph, String className, List<MyMethod> methods, Map<String, Integer> clusters, int clusterId) {
        MyClass classA = new MyClass(className);
        classA.getMethods().addAll(methods);
        myGraph.getGraph().addVertex(classA);
        myGraph.getClasses().put(classA.getQualifiedName(), classA);
        clusters.put(classA.getQualifiedName(), clusterId);
        return classA;
    }

    @Test
    public void shouldCalculateSingleClusterCHM() {
        MyGraph graph = new MyGraph();
        Map<String, Integer> clusters = new HashMap<>();

        // Add nodes
        MyClass classA = createClassOnGraph(graph, "ClassA",
                Arrays.asList(new MyMethod("fooA", Arrays.asList("String", "Integer", "Visit", "Pet"), "Visit")),
                clusters, 0);
        MyClass classB = createClassOnGraph(graph, "ClassB",
                Arrays.asList(new MyMethod("fooB", Arrays.asList("String", "Integer", "Visit", "Pet"), "Visit")),
                clusters, 1);

        // Add edges
        graph.getGraph().addEdge(classA, classB, new DependencyEdge(""));

        Metric CHM = new CHM(graph);
        double chm = CHM.calculateCluster(clusters);
        assertEquals(1, chm, 0.01);
    }

    @Test
    public void calculationShouldIgnoreClassesWithoutDependenciesOnCluster() {
        MyGraph graph = new MyGraph();
        Map<String, Integer> clusters = new HashMap<>();

        MyClass classA = createClassOnGraph(graph, "ClassA",
                Arrays.asList(new MyMethod("fooA", Arrays.asList("String", "Integer", "Visit", "Pet"), "Visit")),
                clusters, 0);
        MyClass classB = createClassOnGraph(graph, "ClassB",
                Arrays.asList(new MyMethod("fooB", Arrays.asList("String", "Integer", "Visit", "Pet"), "Visit")),
                clusters, 0);

        MyClass classC = createClassOnGraph(graph, "ClassC",
                Arrays.asList(new MyMethod("fooC", Arrays.asList("String", "Integer", "Visit", "Pet"), "Visit")),
                clusters, 0);

        MyClass classD = createClassOnGraph(graph, "ClassD",
                Arrays.asList(new MyMethod("fooD", Arrays.asList(), "Visit")),
                clusters, 0);

        // Add edges
        graph.getGraph().addEdge(classA, classB, new DependencyEdge(""));
        graph.getGraph().addEdge(classA, classC, new DependencyEdge(""));
        graph.getGraph().addEdge(classB, classC, new DependencyEdge(""));
        graph.getGraph().addEdge(classB, classA, new DependencyEdge(""));

        Metric CHM = new CHM(graph);
        double chm = CHM.calculateCluster(clusters);
        assertEquals(1, chm, 0.01);
    }

    @Test
    public void calculationShouldIgnoreClassesWithoutMethods() {
        MyGraph graph = new MyGraph();
        Map<String, Integer> clusters = new HashMap<>();

        MyClass classA = createClassOnGraph(graph, "ClassA", new ArrayList(), clusters, 0);
        MyClass classB = createClassOnGraph(graph, "ClassB", new ArrayList(), clusters, 0);

        graph.getGraph().addEdge(classA, classB, new DependencyEdge(""));

        Metric CHM = new CHM(graph);
        double chm = CHM.calculateCluster(clusters);
        assertEquals(1, chm, 0.01);
    }

    @Test
    public void calculationShouldIgnore() {
        MyGraph graph = new MyGraph();
        Map<String, Integer> clusters = new HashMap<>();

        MyClass classA = createClassOnGraph(graph, "ClassA", new ArrayList(), clusters, 0);
        MyClass classB = createClassOnGraph(graph, "ClassB", new ArrayList(), clusters, 0);
        MyClass classC = createClassOnGraph(graph, "ClassC", new ArrayList(), clusters, 1);
        MyClass classD = createClassOnGraph(graph, "ClassD",
                Arrays.asList(new MyMethod("foo", Arrays.asList("Param"), "ReturnType")), clusters, 1);

        graph.getGraph().addEdge(classA, classB, new DependencyEdge(""));
        graph.getGraph().addEdge(classC, classD, new DependencyEdge(""));

        Metric CHM = new CHM(graph);
        double chm = CHM.calculateCluster(clusters);
        assertEquals(.5, chm, 0.01);
    }

    @Test
    public void calculationShouldIgnoreClusterWithoutEdges() {
        MyGraph graph = new MyGraph();
        Map<String, Integer> clusters = new HashMap<>();

        MyClass classA = createClassOnGraph(graph, "ClassA",
                Arrays.asList(new MyMethod("fooA", Arrays.asList("String", "Integer", "Visit", "Pet"), "Visit")),
                clusters, 0);
        MyClass classB = createClassOnGraph(graph, "ClassB",
                Arrays.asList(new MyMethod("fooB", Arrays.asList("String", "Integer", "Visit", "Pet"), "Visit")),
                clusters, 0);

        MyClass classC = createClassOnGraph(graph, "ClassC",
                Arrays.asList(new MyMethod("fooC", Arrays.asList("String", "Integer", "Visit", "Pet"), "Visit")),
                clusters, 1);
        MyClass classD = createClassOnGraph(graph, "ClassD",
                Arrays.asList(new MyMethod("fooD", Arrays.asList("String", "Integer", "Visit", "Pet"), "Visit")),
                clusters, 1);

        // Add edges
        graph.getGraph().addEdge(classA, classB, new DependencyEdge(""));

        Metric CHM = new CHM(graph);
        double chm = CHM.calculateCluster(clusters);
        assertEquals(1, chm, 0.01);
    }

    @Test
    public void calculationShouldIgnoreBothClustersWithoutEdges() {
        MyGraph graph = new MyGraph();
        Map<String, Integer> clusters = new HashMap<>();

        MyClass classA = createClassOnGraph(graph, "ClassA",
                Arrays.asList(new MyMethod("fooA", Arrays.asList("String", "Integer", "Visit", "Pet"), "Visit")),
                clusters, 0);
        MyClass classB = createClassOnGraph(graph, "ClassB",
                Arrays.asList(new MyMethod("fooB", Arrays.asList("String", "Integer", "Visit", "Pet"), "Visit")),
                clusters, 0);

        MyClass classC = createClassOnGraph(graph, "ClassC",
                Arrays.asList(new MyMethod("fooC", Arrays.asList("String", "Integer", "Visit", "Pet"), "Visit")),
                clusters, 1);
        MyClass classD = createClassOnGraph(graph, "ClassD",
                Arrays.asList(new MyMethod("fooD", Arrays.asList("String", "Integer", "Visit", "Pet"), "Visit")),
                clusters, 1);

        Metric CHM = new CHM(graph);
        double chm = CHM.calculateCluster(clusters);
        assertEquals(1, chm, 0.01);
    }

    @Test
    public void calculationShouldCalculateClustersIndependently() {
        MyGraph graph = new MyGraph();
        Map<String, Integer> clusters = new HashMap<>();

        MyClass classA = createClassOnGraph(graph, "ClassA",
                Arrays.asList(new MyMethod("fooA", Arrays.asList("String", "Integer", "Visit", "Pet"), "Visit")),
                clusters, 0);
        MyClass classB = createClassOnGraph(graph, "ClassB",
                Arrays.asList(new MyMethod("fooB", Arrays.asList("String", "Integer", "Visit", "Pet"), "Visit")),
                clusters, 0);

        MyClass classC = createClassOnGraph(graph, "ClassC",
                Arrays.asList(new MyMethod("fooC", Arrays.asList("String", "Integer", "Visit", "Pet"), "Visit")),
                clusters, 1);
        MyClass classD = createClassOnGraph(graph, "ClassD",
                Arrays.asList(new MyMethod("fooD", Arrays.asList("String", "Integer", "Visit", "Pet"), "Visit")),
                clusters, 1);

        // Add edges
        graph.getGraph().addEdge(classA, classB, new DependencyEdge(""));
        graph.getGraph().addEdge(classC, classD, new DependencyEdge(""));

        Metric CHM = new CHM(graph);
        double chm = CHM.calculateCluster(clusters);
        assertEquals(1, chm, 0.01);
    }



}

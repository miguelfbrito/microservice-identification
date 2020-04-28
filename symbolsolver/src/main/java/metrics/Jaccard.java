package metrics;

import graph.DependencyEdge;
import graph.entities.MyClass;
import org.jgrapht.Graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Jaccard {

    public static double calculate(Set<String> source, Set<String> target) {
        Set<String> union = new HashSet<>(source);
        Set<String> intersection = new HashSet<>(source);

        union.addAll(target);
        intersection.retainAll(target);


        return !intersection.isEmpty() ? (double) intersection.size() / union.size() : 0;
    }

    public static Set<String> getUnion(Set<String> source, Set<String> target) {
        Set<String> union = new HashSet<>(source);
        union.addAll(target);
        return union;
    }

    public static Set<String> getIntersection(Set<String> source, Set<String> target) {
        Set<String> intersection = new HashSet<>(source);
        intersection.retainAll(target);
        return intersection;
    }

}

package metrics;

import graph.DependencyEdge;
import graph.entities.MyClass;
import org.jgrapht.Graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class JaccardCoefficient {

    public static double calculate(Set<String> source, Set<String> target) {
        Set<String> union = new HashSet<>(source);
        Set<String> intersection = new HashSet<>(source);

        union.addAll(target);
        intersection.retainAll(target);

        return !union.isEmpty() ? (double) intersection.size() / union.size() : 0;
    }


}

package graph.entities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Service {

    private static AtomicInteger clusterIdCounter = new AtomicInteger(0);

    private Map<String, MyClass> classes;
    // TODO: Consider change to a set of Strings. Could be useful to display service operations
    private int operations;
    private int clusterId;

    public Service() {
        this.classes = new HashMap<>();
        this.operations = 0;
        this.clusterId = clusterIdCounter.getAndIncrement();
    }

    public Map<String, MyClass> getClasses() {
        return classes;
    }

    public void setClasses(Map<String, MyClass> classes) {
        this.classes = classes;
    }

    public int getClusterId() {
        return clusterId;
    }

    public int getOperations() {
        return operations;
    }

    public void setOperations(int operations) {
        this.operations = operations;
    }
}

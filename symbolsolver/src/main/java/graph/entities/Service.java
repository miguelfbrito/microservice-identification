package graph.entities;

import java.util.HashMap;
import java.util.Map;

public class Service {

    private Map<String, MyClass> classes;
    // TODO: Consider changing to a set of Strings. Could be useful to display service operations
    private int operations;
    private int id;

    public Service(int id) {
        this.classes = new HashMap<>();
        this.operations = 0;
        this.id = id;
    }

    public Map<String, MyClass> getClasses() {
        return classes;
    }

    public void setClasses(Map<String, MyClass> classes) {
        this.classes = classes;
    }

    public int getId() {
        return id;
    }

    public int getOperations() {
        return operations;
    }

    public void setOperations(int operations) {
        this.operations = operations;
    }

    @Override
    public String toString() {
        return "Service{" +
                "classes=" + classes +
                ", operations=" + operations +
                ", id=" + id +
                '}';
    }
}

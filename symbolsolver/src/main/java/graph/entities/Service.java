package graph.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Service {

    private Map<String, MyClass> classes;
    private Map<String, String> operations; // <name, correspondingClass>
    private int id;

    public Service(int id) {
        this.classes = new HashMap<>();
        this.operations = new HashMap<>();
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

    public void setOperations(Map<String, String> operations) {
        this.operations = operations;
    }

    public Map<String, String> getOperations() {
        return operations;
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

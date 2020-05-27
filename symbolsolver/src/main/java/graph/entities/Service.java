package graph.entities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Service {

    private Map<String, MyClass> classes;
    private Map<String, String> operations; // <name, correspondingClass>
    private int id;

    // Metrics
    private double chm;
    private double chd;

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

    public double getChm() {
        return chm;
    }

    public void setChm(double chm) {
        this.chm = chm;
    }

    public double getChd() {
        return chd;
    }

    public void setChd(double chd) {
        this.chd = chd;
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

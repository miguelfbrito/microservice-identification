package parser;

import graph.entities.MyClass;
import graph.entities.Service;

import java.util.Map;

public class ParseResult {

    private Map<String, MyClass> classes;
    private Map<Integer, Service> services;

    public ParseResult(Map<String, MyClass> classes, Map<Integer, Service> services) {
        this.classes = classes;
        this.services = services;
    }

    public Map<String, MyClass> getClasses() {
        return classes;
    }

    public Map<Integer, Service> getServices() {
        return services;
    }

    @Override
    public String toString() {
        return "ParseResult{" +
                "classes=" + classes +
                ", services=" + services +
                '}';
    }
}

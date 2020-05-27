package parser;

import graph.entities.MyClass;
import graph.entities.Service;
import projects.Project;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ParseResultServices implements Serializable {

    private Project project;
    private Map<String, MyClass> classes;
    private Map<Integer, Service> services;

    public ParseResultServices(Project project, Map<String, MyClass> classes, Map<Integer, Service> services) {
        this.project = project;
        this.classes = classes;
        this.services = services;
    }

    public ParseResultServices(Map<String, MyClass> classes, Map<Integer, Service> services) {
        this.classes = classes;
        this.services = services;
    }

    public ParseResultServices(ParseResultServices parseResultServices){
        this.project = parseResultServices.getProject();
        this.classes = new HashMap<>(parseResultServices.getClasses());
        this.services = new HashMap<>(parseResultServices.getServices());
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
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

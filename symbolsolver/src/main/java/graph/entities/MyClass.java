package graph.entities;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import java.util.*;

public class MyClass {
    private String simpleName;
    private String qualifiedName;
    private ClassOrInterfaceDeclaration visitor;
    // TODO: consider changint to set
    private Map<String, MyMethod> methods;
    private Set<String> operations; // List of methods called from another service
    private Service service;

    /**
     * Should only be used as a mean to find a match in the graph through hashing
     *
     * @param qualifiedName
     */
    public MyClass(String qualifiedName) {
        this.qualifiedName = qualifiedName;
        this.methods = new HashMap<>();
        this.operations = new HashSet<>();
        this.service = null;
    }

    public MyClass(String qualifiedName, Service service) {
        this.qualifiedName = qualifiedName;
        this.methods = new HashMap<>();
        this.operations = new HashSet<>();
        this.service = service;
    }

    public MyClass(ClassOrInterfaceDeclaration visitor) {
        this.visitor = visitor;
        visitor.getFullyQualifiedName().ifPresent(qualifiedName -> this.qualifiedName = qualifiedName);
        this.simpleName = visitor.getName().toString();
        this.methods = new HashMap<>();
        this.operations = new HashSet<>();
        this.service = null;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public ClassOrInterfaceDeclaration getVisitor() {
        return visitor;
    }

    public Map<String, MyMethod> getMethods() {
        return methods;
    }

    public void setMethods(Map<String, MyMethod> methods) {
        this.methods = methods;
    }

    public Set<String> getOperations() {
        return operations;
    }

    public void setOperations(Set<String> operations) {
        this.operations = operations;
    }

    public void setVisitor(ClassOrInterfaceDeclaration visitor) {
        this.visitor = visitor;
    }

    public boolean isServiceInterface() {
        return !operations.isEmpty();
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        if (service.getClasses().containsKey(qualifiedName)) {
            this.service = service;
        }
    }

    @Override
    public String toString() {
        return "MyClass{" +
                "qualifiedName='" + qualifiedName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyClass myClass = (MyClass) o;
        return qualifiedName.equals(myClass.qualifiedName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(qualifiedName);
    }
}

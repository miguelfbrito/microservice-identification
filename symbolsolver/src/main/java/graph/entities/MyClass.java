package graph.entities;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import java.util.*;

public class MyClass {
    private String simpleName;
    private String qualifiedName;
    private ClassOrInterfaceDeclaration visitor;
    // TODO: consider changint to set
    private List<MyMethod> methods;
    private Set<String> operations; // List of methods called from another service

    /**
     * Should only be used as a mean to find a match in the graph through hashing
     * @param qualifiedName
     */
    public MyClass(String qualifiedName){
        this.qualifiedName = qualifiedName;
        this.methods = new ArrayList<>();
        this.operations = new HashSet<>();
    }

    public MyClass(ClassOrInterfaceDeclaration visitor) {
        this.visitor = visitor;
        visitor.getFullyQualifiedName().ifPresent(qualifiedName -> this.qualifiedName = qualifiedName);
        this.simpleName = visitor.getName().toString();
        this.methods = new ArrayList<>();
        this.operations = new HashSet<>();
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

    public List<MyMethod> getMethods() {
        return methods;
    }

    public void setMethods(List<MyMethod> methods) {
        this.methods = methods;
    }

    public Set<String> getOperations() {
        return operations;
    }

    public void setOperations(Set<String> operations) {
        this.operations = operations;
    }

    public boolean isServiceInterface() {
        return !operations.isEmpty();
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

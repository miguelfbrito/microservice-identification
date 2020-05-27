package graph.entities;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.google.gson.annotations.Expose;
import javassist.expr.MethodCall;

import java.io.Serializable;
import java.util.*;

public class MyClass {
    private String simpleName;
    private String qualifiedName;
    private ClassOrInterfaceDeclaration visitor;
    private List<MarkerAnnotationExpr> annotations;
    private List<VariableDeclarator> variables;
    private Map<String, MyMethod> methods; // method name, method reference
    private List<MethodCallExpr> methodInvocations;
    private Set<String> operations; // List of methods called from another service, used for metric calculation
    private Set<String> implementedTypes;
    private Set<String> extendedTypes;
    private Service service;

    public MyClass(String qualifiedName) {
        this.qualifiedName = qualifiedName;
        this.methods = new HashMap<>();
        this.operations = new HashSet<>();
        this.implementedTypes = new HashSet<>();
        this.extendedTypes = new HashSet<>();
    }

    public MyClass(String qualifiedName, Service service) {
        this(qualifiedName);
        this.service = service;
    }

    public MyClass(ClassOrInterfaceDeclaration visitor) {
        this.methods = new HashMap<>();
        this.operations = new HashSet<>();
        this.implementedTypes = new HashSet<>();
        this.extendedTypes = new HashSet<>();
        this.visitor = visitor;
        visitor.getFullyQualifiedName().ifPresent(qualifiedName -> this.qualifiedName = qualifiedName);
        this.simpleName = visitor.getName().toString();
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

    public Set<String> getImplementedTypes() {
        return implementedTypes;
    }

    public void setImplementedTypes(Set<String> implementedTypes) {
        this.implementedTypes = implementedTypes;
    }

    public Set<String> getExtendedTypes() {
        return extendedTypes;
    }

    public void setExtendedTypes(Set<String> extendedTypes) {
        this.extendedTypes = extendedTypes;
    }

    public List<MethodCallExpr> getMethodInvocations() {
        return methodInvocations;
    }

    public void setMethodInvocations(List<MethodCallExpr> methodInvocations) {
        this.methodInvocations = methodInvocations;
    }

    @Override
    public String toString() {
        return "MyClass{" +
                "qualifiedName='" + qualifiedName + '\'' +
                '}';
    }

    public List<MarkerAnnotationExpr> getAnnotations() {
        return annotations;
    }

    public List<VariableDeclarator> getVariables() {
        return variables;
    }

    public void setVariables(List<VariableDeclarator> variables) {
        this.variables = variables;
    }

    public void setAnnotations(List<MarkerAnnotationExpr> annotations) {
        this.annotations = annotations;
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

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

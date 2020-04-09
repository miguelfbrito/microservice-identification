package graph;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitor;

import java.util.Objects;

public class MyClass {
    private String simpleName;
    private String qualifiedName;
    private ClassOrInterfaceDeclaration visitor;

    /**
     * Should only be used as a mean to find a match in the graph through hashing
     * @param qualifiedName
     */
    public MyClass(String qualifiedName){
        this.qualifiedName = qualifiedName;
    }

    public MyClass(ClassOrInterfaceDeclaration visitor) {
        this.visitor = visitor;
        visitor.getFullyQualifiedName().ifPresent(qualifiedName -> this.qualifiedName = qualifiedName);
        this.simpleName = visitor.getName().toString();
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

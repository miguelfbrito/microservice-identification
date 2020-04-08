package graph;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitor;

public class MyClass {
    private String simpleName;
    private String qualifiedName;
    private ClassOrInterfaceDeclaration visitor;

    public MyClass(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public MyClass(ClassOrInterfaceDeclaration visitor) {
        this.visitor = visitor;
    }

    public MyClass(String simpleName, String qualifiedName) {
        this.simpleName = simpleName;
        this.qualifiedName = qualifiedName;
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
}

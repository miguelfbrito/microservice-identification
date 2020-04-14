package graph.creation;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import graph.entities.MyClass;
import visitors.ClassOrInterfaceDeclarationVisitor;

import java.util.*;

public class ClassIdentifier {
    public Map<String, MyClass> identify(List<CompilationUnit> compilationUnits){
        Map<String, MyClass> classes = new HashMap<>();
        Set<ClassOrInterfaceDeclaration> nodes = new HashSet<>();
        ClassOrInterfaceDeclarationVisitor classOrInterfaceDeclarationVisitor = new ClassOrInterfaceDeclarationVisitor();

        for (CompilationUnit cu : compilationUnits) {
            cu.accept(classOrInterfaceDeclarationVisitor, nodes);
            for (ClassOrInterfaceDeclaration node : nodes) {
                MyClass myClass = new MyClass(node);
                node.getFullyQualifiedName().ifPresent(name -> classes.put(name, myClass));
                // graph.addVertex(myClass);
            }
            nodes.clear();
        }

        return classes;
    }


}

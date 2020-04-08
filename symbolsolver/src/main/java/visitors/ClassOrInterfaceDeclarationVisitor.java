package visitors;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.Set;

public class ClassOrInterfaceDeclarationVisitor extends VoidVisitorAdapter<Set> {

        @Override
        public void visit(ClassOrInterfaceDeclaration visitor, Set collector) {
                super.visit(visitor, collector);
                collector.add(visitor);
        }
}

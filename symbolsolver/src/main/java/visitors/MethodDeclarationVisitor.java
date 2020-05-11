package visitors;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.List;
import java.util.Set;

public class MethodDeclarationVisitor extends VoidVisitorAdapter<List> {

        @Override
        public void visit(MethodDeclaration visitor, List collector) {
                super.visit(visitor, collector);
                collector.add(visitor);
        }
}

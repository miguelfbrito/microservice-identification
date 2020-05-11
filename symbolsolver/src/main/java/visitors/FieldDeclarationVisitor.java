package visitors;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.Set;

public class FieldDeclarationVisitor extends VoidVisitorAdapter<Set> {

        @Override
        public void visit(FieldDeclaration visitor, Set collector) {
                super.visit(visitor, collector);
                collector.add(visitor);
        }
}

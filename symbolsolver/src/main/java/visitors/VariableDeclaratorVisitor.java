package visitors;

import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.List;
import java.util.Set;

public class VariableDeclaratorVisitor extends VoidVisitorAdapter<List> {

        @Override
        public void visit(VariableDeclarator visitor, List collector) {
                super.visit(visitor, collector);
                collector.add(visitor);
        }
}

package visitors;

import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.Set;

public class AnnotationVisitor extends VoidVisitorAdapter<Set> {

        @Override
        public void visit(MarkerAnnotationExpr visitor, Set collector) {
                super.visit(visitor, collector);
                collector.add(visitor);
        }
}

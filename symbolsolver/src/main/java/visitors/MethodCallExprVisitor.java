package visitors;

import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MethodCallExprVisitor extends VoidVisitorAdapter<List> {

    @Override
    public void visit(MethodCallExpr visitor, List collector) {
        super.visit(visitor, collector);
        collector.add(visitor);
    }
}

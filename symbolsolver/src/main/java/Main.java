import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparser.Navigator;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.ParserCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

class ObtainMethodCalssInsideMethod {
    @Test
    public void test() {

        CompilationUnit cu = StaticJavaParser.parse("class X{ public void testTagDefinitions() { Facade facade = Model.getFacade(); Object td = Model.getExtensionMechanismsFactory().buildTagDefinition(\"testTD\", stereotype, null, \"String\"); assertEquals(\"1..3\", facade.toString(facade.getMultiplicity(td))); }}");
        Optional<ClassOrInterfaceDeclaration> classX = cu.getClassByName("X");

        for (MethodDeclaration method : classX.get().getMethods()) {
            // Make the visitor go through everything inside the method.
            // method.accept(new MethodCallVisitor(), null);
        }
    }

}

class MethodNamePrinter extends VoidVisitorAdapter<Void> {

    @Override
    public void visit(MethodDeclaration md, Void arg) {
        super.visit(md, arg);
        System.out.println("Method Name Printed: " + md.getName());
    }
}


import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.ParserCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class SymbolSolver {

    @Test
    public void parseWholeProject() {
        //https://javaparser.org/setting-up-for-analysing-a-whole-project/
        String projectName = "test";
        String root = "/home/mbrito/git/thesis-web-applications/monoliths/" + projectName;
        Path path = Path.of(root);
        final ProjectRoot projectRoot = new ParserCollectionStrategy().collect(path);

        List<ParseResult<CompilationUnit>> parseResults = null;

        TypeSolver reflectionTypeSolver = new ReflectionTypeSolver();
        TypeSolver typeSolver = new CombinedTypeSolver();
        TypeSolver javaParserTypeSolver = new JavaParserTypeSolver(projectRoot.getRoot());
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(reflectionTypeSolver);
        combinedTypeSolver.add(javaParserTypeSolver);
/*
        combinedTypeSolver.add(typeSolver);
*/
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);


        for (SourceRoot sr : projectRoot.getSourceRoots()) {
            System.out.println(sr);
            try {
                sr.getParserConfiguration().setSymbolResolver(symbolSolver);
                parseResults = sr.tryToParse();

                for (ParseResult<CompilationUnit> parseResult : parseResults) {
                    Optional<CompilationUnit> result = parseResult.getResult();
                    if (result.isPresent()) {
                        CompilationUnit cu = result.get();
                        System.out.println(cu);
                        cu.findAll(AssignExpr.class).forEach(ae -> {
                            ResolvedType resolvedType = ae.calculateResolvedType();
                            System.out.println(ae.toString() + " is a : " + resolvedType);
                        });

                        List<MethodCallExpr> methodCallExprs = cu.findAll(MethodCallExpr.class);

                        for (MethodCallExpr mce : methodCallExprs) {
                            String qualifiedName = mce.resolve().toString();
                            System.out.println("\n\nField type: " + qualifiedName);
                            System.out.println(mce.toString());
                        }
/*
                        }
                VoidVisitor<?> methodNameVisitor = new MethodNamePrinter();
                methodNameVisitor.visit(result.get(), null);
*/
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

}

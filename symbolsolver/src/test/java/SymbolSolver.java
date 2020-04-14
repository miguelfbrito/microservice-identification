import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.ParserCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class SymbolSolver {

    @Test
    public void parseProject() throws IOException {

        // String projectName = "test";
        // String projectName = "simple-blog";
        String projectName = "monomusiccorp";
        String root = "/home/mbrito/git/thesis-web-applications/monoliths/" + projectName;
        final ProjectRoot projectRoot = new ParserCollectionStrategy().collect(Path.of(root));

        System.out.println(projectRoot.getSourceRoots());
        for(SourceRoot sr : projectRoot.getSourceRoots()){

            // The SymbolSolver has to receive a SourceRoot instead of a Project Root!
            CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
            combinedTypeSolver.add(new ReflectionTypeSolver());
            combinedTypeSolver.add(new JavaParserTypeSolver(sr.getRoot()));
            JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);

            sr.getParserConfiguration().setSymbolResolver(symbolSolver);
            List<ParseResult<CompilationUnit>> parseResults = sr.tryToParse();

            for(ParseResult<CompilationUnit> parseResult : parseResults){
                parseResult.getResult().ifPresent(compilationUnit -> {
                    compilationUnit.findAll(MethodCallExpr.class).forEach(methodCall -> {
                        // getScope() shouldn't be needed according to documentation and examples
                        // However, took me a whole day to figure out it must be included in MethodCallExpr, works fine for simpler types.
                        methodCall.getScope().ifPresent(rs -> {
                            try {
                                ResolvedType resolvedType = rs.calculateResolvedType();
                            } catch(Exception e){
                                 // System.out.println("[UnsolvedSymbolException] on " + rs.toString());
                            }
                        });
                    });
                });
            }
        }
    }
}

package parser;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.ParserCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Parser {

    public List<CompilationUnit> parseProject(Path path) throws IOException, IOException {

        final ProjectRoot projectRoot = new ParserCollectionStrategy().collect(path);
        final List<CompilationUnit> compilationUnits = new ArrayList<>();

        for (SourceRoot sr : projectRoot.getSourceRoots()) {

            // The SymbolSolver has to receive a SourceRoot instead of a Project Root!
            CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
            combinedTypeSolver.add(new ReflectionTypeSolver());
            combinedTypeSolver.add(new JavaParserTypeSolver(sr.getRoot()));
            JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);

            sr.getParserConfiguration().setSymbolResolver(symbolSolver);
            List<ParseResult<CompilationUnit>> parseResults = sr.tryToParse();

            for (ParseResult<CompilationUnit> parseResult : parseResults) {
                parseResult.getResult().ifPresent(compilationUnit -> {
                    compilationUnits.add(compilationUnit);

                    compilationUnit.findAll(MethodCallExpr.class).forEach(methodCall -> {
                        // getScope() shouldn't be needed according to documentation and examples
                        // However, took me a whole day to figure out it must be included in MethodCallExpr, works fine for simpler types.
                        methodCall.getScope().ifPresent(rs -> {
                            try {
                                ResolvedType resolvedType = rs.calculateResolvedType();
                            } catch (Exception e) {
                                // System.out.println("[UnsolvedSymbolException] on " + rs.toString());
                            }
                        });
                    });
                });
            }
        }

        return compilationUnits;
    }
}

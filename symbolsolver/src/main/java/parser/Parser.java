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
            //combinedTypeSolver.add(new ReflectionTypeSolver());
            combinedTypeSolver.add(new JavaParserTypeSolver(sr.getRoot()));
            JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);

            sr.getParserConfiguration().setSymbolResolver(symbolSolver);
            List<ParseResult<CompilationUnit>> parseResults = sr.tryToParse();

            for (ParseResult<CompilationUnit> parseResult : parseResults) {
                parseResult.getResult().ifPresent(compilationUnits::add);
            }
        }

        return compilationUnits;
    }
}

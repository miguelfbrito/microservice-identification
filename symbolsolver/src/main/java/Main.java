import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import graph.MyGraph;
import parser.Parser;
import visitors.ClassOrInterfaceDeclarationVisitor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {

    public static void main(String[] args) {

        String projectName = "test";
        String path = "/home/mbrito/git/thesis-web-applications/monoliths/" + projectName;

        Set<String> qualifiedNames = new HashSet<>();
        Parser parser = new Parser();
        List<CompilationUnit> compilationUnits = null;
        try {
            compilationUnits = parser.parseProject(Path.of(path));
            for(CompilationUnit cu : compilationUnits){
                cu.accept(new ClassOrInterfaceDeclarationVisitor(), qualifiedNames);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        MyGraph<String> graph = new MyGraph<>();
        graph.create(compilationUnits);

    }
}



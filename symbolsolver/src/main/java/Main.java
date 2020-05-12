import com.github.javaparser.ast.CompilationUnit;
import parser.Parse;
import parser.Parser;
import utils.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {

        final String PROJECTS_ROOT = "/home/mbrito/git/thesis-web-applications/monoliths";
        final String project_name = "/spring-blog";
        // final String project_name = "/spring-blog";

        for (String s : args) {
            System.out.println(s);
        }
        Parser parser = new Parser();
        List<CompilationUnit> compilationUnits = parser.parseProject(Path.of(PROJECTS_ROOT + project_name));
        Parse parse = new Parse();
        parse.completeParse(compilationUnits);


    }
}



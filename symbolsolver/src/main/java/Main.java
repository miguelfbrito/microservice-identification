import com.github.javaparser.ast.CompilationUnit;
import parser.Parse;
import parser.Parser;
import utils.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {

        final String PROJECTS_ROOT = "/mnt/HC_Volume_5723285/githubextractor/";

        String project_name = System.getProperty("project");
        if (project_name != null) {
            Parser parser = new Parser();
            List<CompilationUnit> compilationUnits = parser.parseProject(Path.of(PROJECTS_ROOT + project_name));
            Parse parse = new Parse();
            parse.completeParse(compilationUnits);

        }

    }
}



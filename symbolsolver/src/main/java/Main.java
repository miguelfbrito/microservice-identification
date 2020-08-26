import com.github.javaparser.ast.CompilationUnit;
import parser.Parse;
import parser.Parser;
import projects.GenerateMetrics;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        if (System.getProperty("parse") != null) {
            parseProject();
        } else if (System.getProperty("metrics") != null) {
            runMetrics();
        }
    }

    public static void parseProject() throws IOException {
        String projectPath = System.getProperty("project");
        if (projectPath != null) {
            System.out.println("Found project path: " + projectPath);
            Parser parser = new Parser();
            List<CompilationUnit> compilationUnits = parser.parseProject(Path.of(projectPath));
            // TODO : Consider merging Parse with Parser
            Parse parse = new Parse();
            parse.completeParse(compilationUnits);
        }
    }

    public static void runMetrics() {
        GenerateMetrics generateMetrics = new GenerateMetrics();
        generateMetrics.generate();
    }


}



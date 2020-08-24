import com.github.javaparser.ast.CompilationUnit;
import constants.Constants;
import parser.Parse;
import parser.Parser;
import projects.GenerateMetrics;
import projects.ProjectMetrics;
import utils.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println(System.getProperties());

        System.out.println(Constants.DIRECTORY);

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
            Parse parse = new Parse();
            parse.completeParse(compilationUnits);
        }
    }

    public static void runMetrics() {
        GenerateMetrics generateMetrics = new GenerateMetrics();
        generateMetrics.generate();
    }


}



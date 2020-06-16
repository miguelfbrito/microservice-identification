import com.github.javaparser.ast.CompilationUnit;
import constants.Constants;
import parser.Parse;
import parser.Parser;
import projects.GenerateMetrics;
import projects.ProjectMetrics;
import utils.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println(System.getProperties());

        if (System.getProperty("parse") != null) {
            parseProject();
        } else if (System.getProperty("metrics") != null) {
            runMetrics();
        }




    }


    public static void parseProject() throws IOException {
        String project_name = System.getProperty("project");
        if (project_name != null) {
            System.out.println("Found project name " + project_name);
            Parser parser = new Parser();
            List<CompilationUnit> compilationUnits = parser.parseProject(Path.of(Constants.MONOLITHS_DIRECTORY + "/" + project_name));
            Parse parse = new Parse();
            parse.completeParse(compilationUnits);
        }
    }

    public static void runMetrics() {
        GenerateMetrics generateMetrics = new GenerateMetrics();
        generateMetrics.generate();
    }


}



import com.github.javaparser.ast.CompilationUnit;
import constants.Constants;
import parser.Parse;
import parser.Parser;
import projects.MetricsCalculator;

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

        runMetrics();
    }

    public static void parseProject() throws IOException {
        Parser parser = new Parser();
        List<CompilationUnit> compilationUnits = parser.parseProject(Path.of(Constants.PROJECT_PATH));

        // TODO : Consider merging Parse with Parser
        Parse parse = new Parse();
        parse.completeParse(compilationUnits);
    }

    public static void runMetrics() {
        MetricsCalculator metricsCalculator = new MetricsCalculator();
        metricsCalculator.calculate();
    }


}



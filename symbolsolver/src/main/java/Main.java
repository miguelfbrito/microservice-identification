import com.github.javaparser.ast.CompilationUnit;
import constants.Constants;
import stats.ClassStats;
import graph.entities.MyClassDTO;
import stats.ProjectStats;
import parser.Parse;
import parser.Parser;
import projects.MetricsCalculator;
import utils.FileUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws IOException {

/*
        if (System.getProperty("parse") != null) {
            parseProject(Constants.PROJECT_PATH);
        }

        if (System.getProperty("metrics") != null)
            runMetrics();


        if (System.getProperty("stats") != null)
*/

        getProjectStats();

    }

    public static Map<String, MyClassDTO> parseProject(String projectPath) throws IOException {
        Parser parser = new Parser();
        List<CompilationUnit> compilationUnits = parser.parseProject(Path.of(projectPath));

        // TODO : Consider merging Parse with Parser
        Parse parse = new Parse();
        return parse.completeParse(compilationUnits);
    }

    public static void runMetrics() {
        MetricsCalculator metricsCalculator = new MetricsCalculator();
        metricsCalculator.calculate();

    }


    public static void getProjectStats() throws IOException {
        List<String> projects = readProjects();
        String baseDirectory = "/home/mbrito/git/thesis-web-applications/monoliths/";

        for (String project : projects) {
            Map<String, MyClassDTO> parsedClasses = parseProject(baseDirectory + project);
            Map<String, ClassStats> classStats = new HashMap<>();

            for (MyClassDTO myClassDTO : parsedClasses.values()) {
                int methodsPerClass = myClassDTO.getMethods().values().size();
                int methodInvocationsPerClass = myClassDTO.getMethodInvocations().size();
                classStats.put(myClassDTO.getQualifiedName(), new ClassStats(methodsPerClass, methodInvocationsPerClass));

                System.out.println(myClassDTO.getQualifiedName() + " --> " + methodsPerClass + " , "
                        + methodInvocationsPerClass);
            }

            ProjectStats projectStats = new ProjectStats(classStats);
            String filePath = Constants.DIRECTORY + "/data/projectstats/" + project;
            FileUtils.jsonDump(projectStats, filePath);
        }
    }

    public static List<String> readProjects() {
        List<String> projects = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("/home/mbrito/git/thesis/data/projects_list"))) {

            String line = reader.readLine();
            System.out.println("Projects:");

            while (line != null) {
                System.out.println(line);

                projects.add(line);
                line = reader.readLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return projects;
    }

}



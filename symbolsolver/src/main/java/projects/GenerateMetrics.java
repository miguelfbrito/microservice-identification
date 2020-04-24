package projects;

import com.github.javaparser.ast.CompilationUnit;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import graph.MyGraph;
import graph.creation.ByMethodCallInvocation;
import metrics.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import parser.Parse;
import parser.ParseResult;
import parser.Parser;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GenerateMetrics {


    private static String PROJECTS_ROOT;

    public static void checkEnv() {

        if (System.getenv("CI") == null) {
            PROJECTS_ROOT = "/home/mbrito/git/thesis-web-applications/monoliths";
        } else {
            PROJECTS_ROOT = System.getenv("GITHUB_WORKSPACE") + "/thesis-web-applications/monoliths";
        }
    }

    public List<ProjectMetrics> generate() {

        // When running from another test method
        checkEnv();

        List<ProjectMetrics> projectMetrics = new ArrayList<>();
        Gson gson = new Gson();
        try (FileReader reader = new FileReader("../projects.json")) {
            //Read JSON file
            Type projectType = new TypeToken<ArrayList<Project>>() {
            }.getType();

            List<Project> projects = gson.fromJson(reader, projectType);
            projectMetrics = saveMetricsResults(projects);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return projectMetrics;
    }

    public List<ProjectMetrics> saveMetricsResults(List<Project> projects) throws IOException {

        List<ProjectMetrics> projectMetrics = new ArrayList<>();

        for (Project proj : projects) {
            String completePath = PROJECTS_ROOT + "/" + proj.getPath();
            List<CompilationUnit> compilationUnits = new Parser().parseProject(Path.of(completePath));
            Parse parse = new Parse();
            ParseResult parseResult = parse.completeParseClusters(compilationUnits, proj.getClusterString());
            ProjectMetrics pm = new ProjectMetrics(proj);

            pm.setIrn(calculateIRN(parseResult));
            pm.setOpn(calculateOPN(parseResult));
            pm.setChm(calculateCHM(parseResult));
            pm.setChd(calculateCHD(parseResult));

            projectMetrics.add(pm);
            writeToFile(pm);
        }

        return projectMetrics;
    }

    public double calculateIRN(ParseResult parseResult) throws IOException {
        MyGraph graphReference = new ByMethodCallInvocation(parseResult);
        Metric IRN = new IRN(graphReference, parseResult);
        double irn = IRN.calculateService();
        System.out.println("IRN Project: " + irn);
        return irn;
    }

    public double calculateOPN(ParseResult parseResult) throws IOException {
        Metric OPN = new OPN(parseResult);
        double opn = OPN.calculateService();
        System.out.println("OPN Project: " + opn);
        return opn;
    }

    public double calculateCHM(ParseResult parseResult) throws IOException {
        Metric CHM = new CHM(parseResult);
        double chm = CHM.calculateService();
        System.out.println("CHM Project: " + chm);
        return chm;
    }

    public double calculateCHD(ParseResult parseResult) throws IOException {
        Metric CHD = new CHD(parseResult);
        double chd = CHD.calculateService();
        System.out.println("CHD Project: " + chd);
        return chd;
    }

    public void writeToFile(ProjectMetrics metrics) throws IOException {

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String date = formatter.format(new Date());


        // WRITE TO GENERIC FILE
        String path = "../data/results.csv";
        File file = new File(path);
        boolean writeHeader = !file.exists();

        BufferedWriter writer = new BufferedWriter(
                new FileWriter(path, true)  //Set true for append mode
        );

        if (writeHeader) {
            writer.write("ProjectName; IRN; OPN; CHM; CHD; ;Date;");
        }
        String line = metrics.getProject().getName() + ";" + metrics.getIrn() + ";" + metrics.getOpn() +
                ";" + String.format("%.3f", metrics.getChm()) + ";" + String.format("%.3f", metrics.getChd()) + "; ;" + date + ";";

        writer.newLine();
        writer.write(line);
        writer.close();


        // WRITE TO PROJECT SPECIFIC FILE
        path = "../data/results_" + metrics.getProject().getName() + ".csv";
        file = new File(path);
        writeHeader = !file.exists();
        BufferedWriter projectWriter = new BufferedWriter(
                new FileWriter(path, true)  //Set true for append mode
        );

        if (writeHeader) {
            projectWriter.write(metrics.getProject().getName());
            projectWriter.newLine();
            projectWriter.write("IRN; OPN; CHM; CHD; ;Date;");
        }

        String projectLine = metrics.getIrn() + ";" + metrics.getOpn() +
                ";" + String.format("%.3f", metrics.getChm()) + ";" + String.format("%.3f", metrics.getChd()) + "; ;" + date + ";";

        projectWriter.newLine();
        projectWriter.write(projectLine);
        projectWriter.close();
    }

}

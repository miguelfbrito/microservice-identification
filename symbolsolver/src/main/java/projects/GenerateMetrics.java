package projects;

import com.github.javaparser.ast.CompilationUnit;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import graph.MyGraph;
import graph.creation.ByMethodCallInvocation;
import graph.entities.MyClass;
import graph.entities.Service;
import metrics.*;
import parser.Parse;
import parser.ParseResultServices;
import parser.Parser;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class GenerateMetrics {


    private static String PROJECTS_ROOT;

    public static void checkEnv() {

        if (System.getenv("CI") == null) {
            PROJECTS_ROOT = "/home/mbrito/git/thesis-web-applications/monoliths";
        } else {
            PROJECTS_ROOT = System.getenv("GITHUB_WORKSPACE") + "/thesis-web-applications/monoliths";
        }
    }

    public static void extractClustersToFile(Map<Integer, Service> services, Project project) throws IOException {

        // WRITE TO GENERIC FILE
        String path = "../data/clusters/" + project.getName();
        BufferedWriter writer = new BufferedWriter(
                new FileWriter(path)  //Set true for append mode
        );

        for (Service service : services.values()) {
            writer.write("Total service operations: " + service.getOperations().size());
            writer.newLine();
            for (MyClass myClass : service.getClasses().values()) {
                writer.write(myClass.getQualifiedName());
                writer.newLine();
                for (String operation : myClass.getOperations()) {
                    writer.write("\t" + operation);
                    writer.newLine();
                }
            }
            writer.write("--------------------------------------------");
            writer.newLine();
            writer.newLine();
        }

        writer.close();

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
            projectMetrics = calculateMetrics(projects);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return projectMetrics;
    }

    public List<ProjectMetrics> calculateMetrics(List<Project> projects) throws IOException {

        List<ProjectMetrics> projectMetrics = new ArrayList<>();

        for (Project proj : projects) {
            String completePath = PROJECTS_ROOT + "/" + proj.getRelativePath();
            List<CompilationUnit> compilationUnits = new Parser().parseProject(Path.of(completePath));
            Parse parse = new Parse();
            ParseResultServices parseResultServices = parse.completeParseClusters(compilationUnits, proj.getClusterString());
            ProjectMetrics pm = new ProjectMetrics(proj);

            pm.setIrn(calculateIRN(parseResultServices));
            pm.setOpn(calculateOPN(parseResultServices));
            pm.setChm(calculateCHM(parseResultServices));
            pm.setChd(calculateCHD(parseResultServices));

            projectMetrics.add(pm);
            extractClustersToFile(parseResultServices.getServices(), proj);
            writeToFile(pm);
        }

        return projectMetrics;
    }

    public double calculateIRN(ParseResultServices parseResultServices) throws IOException {
        MyGraph graphReference = new ByMethodCallInvocation(parseResultServices);
        Metric IRN = new IRN(graphReference, parseResultServices);
        double irn = IRN.calculateService();
        System.out.println("IRN Project: " + irn);
        return irn;
    }

    public double calculateOPN(ParseResultServices parseResultServices) throws IOException {
        Metric OPN = new OPN(parseResultServices);
        double opn = OPN.calculateService();
        System.out.println("OPN Project: " + opn);
        return opn;
    }

    public double calculateCHM(ParseResultServices parseResultServices) throws IOException {
        Metric CHM = new CHM(parseResultServices);
        double chm = CHM.calculateService();
        System.out.println("CHM Project: " + chm);
        return chm;
    }

    public double calculateCHD(ParseResultServices parseResultServices) throws IOException {
        Metric CHD = new CHD(parseResultServices);
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

        final String header = "IRN; OPN; CHM; CHD; Commit hash ;Date;";

        if (writeHeader) {
            writer.write("ProjectName; " + header);
        }
        String line = metrics.getProject().getName() + ";" + metrics.getIrn() + ";" + metrics.getOpn() + ";" +
                String.format("%.3f", metrics.getChm()) + ";" +
                String.format("%.3f", metrics.getChd()) + ";" + metrics.getProject().getCommitHash() + " ;" + date + ";";

        writer.newLine();
        writer.write(line);
        writer.close();


        // WRITE TO PROJECT SPECIFIC FILE
        path = "../data/results_" + metrics.getProject().getName() + ".csv";
        file = new File(path);
        writeHeader = !file.exists();
        BufferedWriter projectWriter = new BufferedWriter(new FileWriter(path, true));

        if (writeHeader) {
            projectWriter.write(metrics.getProject().getName());
            projectWriter.newLine();
            projectWriter.write(header);
        }

        String projectLine = metrics.getIrn() + ";" + metrics.getOpn() + ";" +
                String.format("%.3f", metrics.getChm()) + ";" +
                String.format("%.3f", metrics.getChd()) + ";" + metrics.getProject().getCommitHash() + " ;" + date + ";";

        projectWriter.newLine();
        projectWriter.write(projectLine);
        projectWriter.close();
    }

}

package projects;

import com.github.javaparser.ast.CompilationUnit;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import graph.DependencyEdge;
import graph.MyGraph;
import graph.creation.ByMethodCallInvocation;
import constants.Constants;
import extraction.ExtractIdentifiedClasses;
import graph.entities.MyClass;
import graph.entities.MyMethod;
import graph.entities.Service;
import metrics.*;
import parser.Parse;
import parser.ParseResultServices;
import parser.Parser;
import utils.FileUtils;
import utils.StringUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

public class MetricsCalculator {

    private static String PROJECTS_ROOT;

    public static void extractClustersToFile(Map<Integer, Service> services, Project project) throws IOException {

        // WRITE TO GENERIC FILE
        String path = Constants.DIRECTORY + "/data/operations_per_service/" + project.getName();
        BufferedWriter writer = new BufferedWriter(
                new FileWriter(path) // Set true for append mode
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

    public List<ProjectMetrics> calculate() {

        List<ProjectMetrics> projectMetrics = new ArrayList<>();
        Gson gson = new Gson();

        String projectDirectory = Constants.DIRECTORY + "/projects.json";
        try (FileReader reader = new FileReader(projectDirectory)) {
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

    public void extractInterfaces(String interfaceFilePath) throws IOException {
        Parser parser = new Parser();
        List<CompilationUnit> compilationUnits = parser.parseProject(Path.of(Constants.PROJECT_PATH));

        Parse parse = new Parse();
        Map<String, MyClass> parseResult = parse.extractClasses(compilationUnits);

        List<String> filters = Arrays.asList("(?i)\\.*controller$");
        ExtractIdentifiedClasses extract = new ExtractIdentifiedClasses();

        List<String> classes = extract.extractFilterBased(new ArrayList<>(parseResult.keySet()), filters);
        classes.forEach(System.out::println);

        try (BufferedWriter bf = new BufferedWriter(new FileWriter(interfaceFilePath))) {
            for (String classe : classes) {
                bf.write(classe + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public List<ProjectMetrics> calculateMetrics(List<Project> projects) throws IOException {

        List<ProjectMetrics> projectMetrics = new ArrayList<>();

        for (Project project : projects) {
            List<CompilationUnit> compilationUnits = new Parser().parseProject(Path.of(Constants.PROJECT_PATH));
            List<String> interfaces = new ArrayList<>();

            String interfaceFilePath = Constants.DIRECTORY + "/data/interfaces/" + project.getName();

            // TODO: This can be moved to be executed conditionally.
            // Large projects will struggle with this approach.
            extractInterfaces(interfaceFilePath);

            System.out.println("Reading interfaces from " + interfaceFilePath);
            try (BufferedReader reader = new BufferedReader(new FileReader(interfaceFilePath))) {
                String line = reader.readLine();
                while (line != null) {
                    interfaces.add(line);
                    line = reader.readLine();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            Parse parse = new Parse();
            ParseResultServices parseResultServices = parse.completeParseClusters(compilationUnits,
                    project.getClusterString());
            parseResultServices.setProject(project);
            ProjectMetrics pm = new ProjectMetrics(project);

            pm.setIrn(calculateIRN(parseResultServices));
            cleanUp(parseResultServices);

            pm.setOpn(calculateOPN(parseResultServices, interfaces));

            Map<Integer, Service> services = parseResultServices.getServices();

            String metricsFile = Constants.DIRECTORY + "/app/metrics/output_fosci.csv";
            System.out.println("Writting metrics to file path: " + metricsFile);
            writeServiceOperationsToFile(services, metricsFile);

            // pm.setChm(calculateCHM(parseResultServices, interfaces));
            // cleanUp(parseResultServices);
            // pm.setChd(calculateCHD(parseResultServices, interfaces));

            projectMetrics.add(pm);
            extractClustersToFile(parseResultServices.getServices(), project);
            writeToFile(pm);
        }

        return projectMetrics;
    }

    public void writeServiceOperationsToFile(Map<Integer, Service> services, String path) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));

        for (Map.Entry<Integer, Service> service : services.entrySet()) {
            for (Map.Entry<String, String> operations : service.getValue().getOperations().entrySet()) {
                MyClass myClass = service.getValue().getClasses().get(operations.getValue());
                MyMethod myMethod = myClass.getMethods().get(operations.getKey());

                List<String> parameters = myMethod.getParametersDataType()
                        .stream()
                        .map(s -> StringUtils.filterAndCleanText(s, Constants.STOP_WORDS))
                        .collect(ArrayList::new, List::addAll, List::addAll);

                List<String> returns = StringUtils.extractVariableType(myMethod.getVisitor().getTypeAsString())
                        .stream()
                        .map(s -> StringUtils.filterAndCleanText(s, Constants.STOP_WORDS))
                        .collect(ArrayList::new, List::addAll, List::addAll);
                String line = service.getKey() + ",\"" + operations.getValue() + "\",\"" + operations.getKey() + "\",\""
                        + String.join(" ", parameters)
                        + "\",\"" + String.join(" ", returns) + "\"";

                writer.write(line);
                writer.newLine();
            }
        }
        writer.close();
    }

    /**
     * Clean up operations mutated in previous calculations. A deep copy would be
     * better but it's a lot of work
     * to deep copy all the objects, and we're only changing the operations. A new
     * instance of operations should be
     * OK for now.
     *
     * @param parseResultServices
     */
    private void cleanUp(ParseResultServices parseResultServices) {

        for (Service service : parseResultServices.getServices().values()) {
            service.setOperations(new HashMap<>());
        }
        for (MyClass classe : parseResultServices.getClasses().values()) {
            classe.setOperations(new HashSet<>());
        }

    }

    public double calculateIRN(ParseResultServices parseResultServices) throws IOException {
        MyGraph graphReference = new ByMethodCallInvocation(parseResultServices);
        Metric IRN = new IRN(graphReference, parseResultServices);
        double irn = IRN.calculateService();
        System.out.println("IRN Project: " + irn);

        // Write call invocations for each service to project
        String path = Constants.DIRECTORY + "/data/services/" + parseResultServices.getProject().getName() + "/"
                + parseResultServices.getProject().getName() + "_" + parseResultServices.getProject().getId();
        List<String> lines = new ArrayList<>(Collections.singletonList("\nMethod invocations between services:"));
        for (DependencyEdge e : graphReference.getGraph().edgeSet()) {
            MyClass src = graphReference.getGraph().getEdgeSource(e);
            MyClass dst = graphReference.getGraph().getEdgeTarget(e);
            // TODO : getService() shouldn't be null here, but there's one occasion of it in
            // jforum project
            // not critical, review if necessary
            if (src.getService() != null && dst.getService() != null &&
                    src.getService().getId() != dst.getService().getId()) {
                lines.add("  Method call: " + src.getQualifiedName() + " -> " + dst.getQualifiedName() + " -> "
                        + e.getValue());
            }
        }

        FileUtils.writeToFile(lines, path, true);
        return irn;
    }

    public double calculateOPN(ParseResultServices parseResultServices, List<String> interfaces) throws IOException {
        Metric OPN = new OPN(parseResultServices, interfaces);
        double opn = OPN.calculateService();
        System.out.println("OPN Project: " + opn);
        return opn;
    }

    public double calculateCHM(ParseResultServices parseResultServices, List<String> interfaces) throws IOException {
        Metric CHM = new CHM(parseResultServices, interfaces);
        double chm = CHM.calculateService();
        System.out.println("CHM Project: " + chm);
        return chm;
    }

    public double calculateCHD(ParseResultServices parseResultServices, List<String> interfaces) throws IOException {
        Metric CHD = new CHD(parseResultServices, new HashSet<>(interfaces));
        double chd = CHD.calculateService();
        System.out.println("CHD Project: " + chd);
        return chd;
    }

    public void writeToFile(ProjectMetrics metrics) throws IOException {
        // TODO : refactor
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String date = formatter.format(new Date());

        // WRITE TO GENERIC FILE
        String path = Constants.DIRECTORY + "/data/results.csv";
        File file = new File(path);
        boolean writeHeader = !file.exists();

        BufferedWriter writer = new BufferedWriter(
                new FileWriter(path, true) // Set true for append mode
        );

        final String header = "IRN; OPN; CHM; CHD; Commit hash ;Date;";

        if (writeHeader) {
            writer.write("ProjectName; " + header);
        }
        String line = metrics.getProject().getName() + ";" + metrics.getIrn() + ";" + metrics.getOpn() + ";" +
                String.format("%.3f", metrics.getChm()) + ";" +
                String.format("%.3f", metrics.getChd()) + ";" + metrics.getProject().getCommitHash() + " ;" + date
                + ";";

        writer.newLine();
        writer.write(line);
        writer.close();

        // WRITE TO PROJECT SPECIFIC FILE
        path = Constants.DIRECTORY + "/data/results_" + metrics.getProject().getName() + ".csv";
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
                String.format("%.3f", metrics.getChd()) + ";" + metrics.getProject().getCommitHash() + " ;" + date
                + ";";

        projectWriter.newLine();
        projectWriter.write(projectLine);
        projectWriter.close();

        // WRITE TO SERVICE FILE
        path = Constants.DIRECTORY + "/data/services/" + metrics.getProject().getName() + "/"
                + metrics.getProject().getName() + "_" + metrics.getProject().getId();
        file = new File(path);
        writeHeader = !file.exists();
        BufferedWriter projectWriterService = new BufferedWriter(new FileWriter(path, true));

        if (writeHeader) {
            projectWriterService.write(metrics.getProject().getName());
            projectWriterService.newLine();
            projectWriterService.write(header);
        }

        String content = "Commit hash: " + metrics.getProject().getCommitHash() + "\nIRN: " + metrics.getIrn()
                + "\nOPN: " + metrics.getOpn();
        /*
         * + "\nCHM: " + metrics.getChm() +
         * "\nCHD: " + metrics.getChd()
         */

        projectWriterService.newLine();
        projectWriterService.write(content);
        projectWriterService.close();
    }

}

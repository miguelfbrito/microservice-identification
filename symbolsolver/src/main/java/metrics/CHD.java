package metrics;

import extraction.ExtractOperations;
import graph.DependencyEdge;
import graph.MyGraph;
import graph.entities.MyClass;
import graph.entities.MyMethod;
import graph.entities.Service;
import org.jgrapht.Graph;
import parser.ParseResult;
import utils.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Calculate the CoHesion at Message level (chd)
 * chd = 1 - (Lack of Cohesion at message level)
 * The message level refers to datatypes of parameters on method declarations and its return datatypes.
 */
public class CHD implements Metric {

    private ParseResult parseResult;
    private boolean includeParameters;

    public CHD(ParseResult parseResult) {
        this.parseResult = parseResult;
        this.includeParameters = true;
    }

    private double calculateJaccardCoefficient(MyMethod source, MyMethod target) {
        Set<String> sourceOperationTerms = new HashSet<>(StringUtils.extractCamelCaseLower(source.getName()));
        Set<String> targetOperationTerms = new HashSet<>(StringUtils.extractCamelCaseLower(target.getName()));

        if(includeParameters){
            for(String s : source.getParametersDataType()){
                sourceOperationTerms.addAll(StringUtils.extractVariableType(s));
            }
            for(String s : source.getParametersDataType()){
                targetOperationTerms.addAll(StringUtils.extractVariableType(s));
            }
        }

        return JaccardCoefficient.calculate(sourceOperationTerms, targetOperationTerms);
    }


    @Override
    public double calculateService() {
        /*
            TODO:
             - Source para destino sempre
             - Fazer bilateralmente
         */

        ExtractOperations.extractAtServiceLevel(parseResult);
        ExtractOperations.extractAllClassOperationsToServiceLevel(parseResult.getServices());
        Map<Integer, Service> services = parseResult.getServices();

        double chd = 0.0;
        for (Service service : services.values()) {

            double serviceJaccard = 0.0;
            for (String sourceOperation : service.getOperations().keySet()) {
                for (String targetOperation : service.getOperations().keySet()) {
                    if (sourceOperation.equals(targetOperation))
                        continue;

                    String sourceClassName = service.getOperations().get(sourceOperation);
                    String targetClassName = service.getOperations().get(targetOperation);

                    MyMethod sourceMethod = parseResult.getClasses().get(sourceClassName).getMethods().get(sourceOperation);
                    MyMethod targetMethod = parseResult.getClasses().get(targetClassName).getMethods().get(targetOperation);


                    if (sourceMethod == null || targetMethod == null) {
                         /*
                            The only case I saw this happen, is when the parser operates on
                            codebases with method invocations to methods without declaration
                         */

                        System.out.println("[chd Source or Target method not found] " + sourceOperation + ", " + targetOperation);
                        continue;
                    }

                    double jaccard = calculateJaccardCoefficient(sourceMethod, targetMethod);
                    serviceJaccard += jaccard;

/*
                    System.out.println("Pair: " + sourceOperation + " - " + targetOperation + " : " + jaccard);
                    System.out.println("\t -- " + sourceClassName + " - " + targetClassName);
*/
                }
            }

            // TODO: Originally stated as != 1, but how do we handle when the method has no operations?
            if (service.getOperations().size() <= 1) {
                serviceJaccard = 1;
            } else {
                int opSize = service.getOperations().size();
                serviceJaccard = opSize != 0 ? serviceJaccard / (double) (opSize * (opSize - 1) / 2) : 0;
            }

            chd += serviceJaccard;

        }


        return chd / services.size();
    }

    public boolean isIncludeParameters() {
        return includeParameters;
    }

    public void setIncludeParameters(boolean includeParameters) {
        this.includeParameters = includeParameters;
    }
}



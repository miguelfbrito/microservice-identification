package metrics;

import extraction.ExtractOperations;
import graph.DependencyEdge;
import graph.entities.MyClass;
import graph.MyGraph;
import graph.entities.MyMethod;
import graph.entities.Service;
import org.jgrapht.Graph;
import parser.ParseResult;

import java.util.*;

/**
 * Calculate the CoHesion at Message level (CHM)
 * CHM = 1 - (Lack of Cohesion at message level)
 * Calculates the similarity between datatypes of method inputs and outputs between classes
 */
public class CHM implements Metric {

    private ParseResult parseResult;

    public CHM(ParseResult parseResult) {
        this.parseResult = parseResult;
    }

    private double calculateJaccardCoefficient(MyMethod source, MyMethod target) {
        // Calculate jaccard coefficient for parameters data types
        Set<String> sourceParameters = new HashSet<>(source.getParametersDataType());
        Set<String> targetParameters = new HashSet<>(target.getParametersDataType());

        double coefficientParameters = JaccardCoefficient.calculate(sourceParameters, targetParameters);

        // Calculate jaccard coefficient for return data types
        Set<String> sourceReturn = new HashSet<>(source.getReturnDataType());
        Set<String> targetReturn = new HashSet<>(target.getReturnDataType());

        double coefficientReturn = JaccardCoefficient.calculate(sourceReturn, targetReturn);

        return (coefficientParameters + coefficientReturn) / 2;
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

        double chm = 0.0;
        for (Service service : services.values()) {
            double serviceJaccard = 0.0;
            for (String sourceOperation : service.getOperations().keySet()) {
                for (String targetOperation : service.getOperations().keySet()) {
                    if (sourceOperation.equals(targetOperation)) {
                        continue;
                    }

                    String sourceClassName = service.getOperations().get(sourceOperation);
                    String targetClassName = service.getOperations().get(targetOperation);

                    MyMethod sourceMethod = parseResult.getClasses().get(sourceClassName).getMethods().get(sourceOperation);
                    MyMethod targetMethod = parseResult.getClasses().get(targetClassName).getMethods().get(targetOperation);


                    if (sourceMethod == null || targetMethod == null) {
                        /*
                            The only case I saw this happen, is when the parser operates on
                            codebases with method invocations to methods without declaration
                         */

                        System.out.println("[CHM Source or Target method not found] " + sourceOperation + ", " + targetOperation);
                        continue;
                    }

                    double jaccard = calculateJaccardCoefficient(sourceMethod, targetMethod);
                    serviceJaccard += jaccard;

                    System.out.println("Pair: " + sourceOperation + " - " + targetOperation + " : " + jaccard);
                    System.out.println("\t -- " + sourceClassName + " - " + targetClassName);
                }
            }

            // TODO: Originally stated as != 1, but how do we handle when the method has no operations?
            if (service.getOperations().size() <= 1) {
                serviceJaccard = 1;
            } else {
                int opSize = service.getOperations().size();
                serviceJaccard = opSize != 0 ? serviceJaccard / (double) (opSize * (opSize - 1) / 2) : 0;
            }

            chm += serviceJaccard;

        }


        return chm / services.size();
    }
}



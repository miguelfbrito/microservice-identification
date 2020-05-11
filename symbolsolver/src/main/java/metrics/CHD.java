package metrics;

import extraction.ExtractOperations;
import graph.entities.MyMethod;
import graph.entities.Service;
import parser.ParseResultServices;
import utils.StringUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Calculate the CoHesion at Message level (chd)
 * chd = 1 - (Lack of Cohesion at message level)
 * The message level refers to datatypes of parameters on method declarations and its return datatypes.
 */
public class CHD implements Metric {

    private ParseResultServices parseResultServices;
    private boolean includeParameters;

    public CHD(ParseResultServices parseResultServices) {
        this.parseResultServices = parseResultServices;
        this.includeParameters = true;
    }

    private double calculateJaccardCoefficient(MyMethod source, MyMethod target) {
        // TODO : Consider the commented approach on RS17 (tosc-interf-dom-cohesion) on handling empty sets?
        Set<String> sourceOperationTerms = new HashSet<>(StringUtils.extractCamelCaseLower(source.getName()));
        Set<String> targetOperationTerms = new HashSet<>(StringUtils.extractCamelCaseLower(target.getName()));

        if (includeParameters) {
            for (String s : source.getParametersDataType()) {
                sourceOperationTerms.addAll(StringUtils.extractVariableType(s));
            }
            for (String s : source.getParametersDataType()) {
                targetOperationTerms.addAll(StringUtils.extractVariableType(s));
            }
        }

        Set<String> union = Jaccard.getUnion(sourceOperationTerms, targetOperationTerms);
        Set<String> intersection = Jaccard.getIntersection(sourceOperationTerms, targetOperationTerms);

        return union.isEmpty() ? 0 : intersection.size() / (double) union.size();
    }


    @Override
    public double calculateService() {
        ExtractOperations.extractAtServiceLevel(parseResultServices);
        ExtractOperations.extractAllClassOperationsToServiceLevel(parseResultServices.getServices());
        Map<Integer, Service> services = parseResultServices.getServices();

        double chd = 0.0;
        int countedServices = 0;
        for (Service service : services.values()) {
            int sourceIndex = -1;
            double serviceJaccard = 0.0;

            Map<String, String> operationsInOrder = service.getOperations();

            if (operationsInOrder.isEmpty()) {
                System.out.println("SKIPPING SERVICE");
                continue;
            }

            countedServices++;

            for (String sourceOperation : operationsInOrder.keySet()) {
                sourceIndex++;

                if (sourceIndex >= operationsInOrder.size() - 1)
                    break;

                int targetIndex = -1;
                for (String targetOperation : operationsInOrder.keySet()) {
                    targetIndex++;

                    if (targetIndex <= sourceIndex || sourceOperation.equals(targetOperation))
                        continue;

                    String sourceClassName = service.getOperations().get(sourceOperation);
                    String targetClassName = service.getOperations().get(targetOperation);

                    MyMethod sourceMethod = parseResultServices.getClasses().get(sourceClassName).getMethods().get(sourceOperation);
                    MyMethod targetMethod = parseResultServices.getClasses().get(targetClassName).getMethods().get(targetOperation);

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
                }
            }

            // TODO: Originally stated as == 1, but how do we handle when the service has no operations?
            if (service.getOperations().size() == 1) {
                serviceJaccard = 1;
            } else {
                int opSize = service.getOperations().size();
                serviceJaccard = opSize != 0 ? serviceJaccard / (double) (opSize * (opSize - 1) / 2) : 0;
            }

            chd += serviceJaccard;
        }


        // TODO : chm / countedServices or services.size();
        return chd / countedServices;
    }

    public boolean isIncludeParameters() {
        return includeParameters;
    }

    public void setIncludeParameters(boolean includeParameters) {
        this.includeParameters = includeParameters;
    }
}



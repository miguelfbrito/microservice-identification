package metrics;

import constants.Constants;
import extraction.ExtractOperations;
import graph.entities.MyMethod;
import graph.entities.Service;
import parser.ParseResultServices;
import utils.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Calculate the CoHesion at Message level (CHM)
 * CHM = 1 - (Lack of Cohesion at message level)
 * Calculates the similarity between datatypes of method inputs and outputs between classes
 */
public class CHM implements Metric {

    private ParseResultServices parseResultServices;
    private List<String> interfaces;

    public CHM(ParseResultServices parseResultServices, List<String> interfaces) {
        this.parseResultServices = parseResultServices;
        this.interfaces = interfaces;
    }

    private double computeEdge(Set<String> union, Set<String> intersection) {
        double coefficient = 0;
        if (union.isEmpty())
            return -1;

        if (!intersection.isEmpty())
            coefficient = intersection.size() / (double) union.size();

        return coefficient;
    }

    private double calculateJaccardCoefficient(MyMethod source, MyMethod target) {

        List<String> sourceParameters = source.getParametersDataType().stream().map(String::toLowerCase).collect(Collectors.toList());
        List<String> targetParameters = target.getParametersDataType().stream().map(String::toLowerCase).collect(Collectors.toList());

        List<String> sourceReturn = StringUtils.extractVariableType(source.getVisitor().getTypeAsString())
                .stream()
                .map(s -> StringUtils.filterAndCleanText(s, Constants.STOP_WORDS))
                .collect(ArrayList::new, List::addAll, List::addAll);

        List<String> targetReturn = StringUtils.extractVariableType(target.getVisitor().getTypeAsString())
                .stream()
                .map(s -> StringUtils.filterAndCleanText(s, Constants.STOP_WORDS))
                .collect(ArrayList::new, List::addAll, List::addAll);

        Set<String> parametersUnion = Jaccard.getUnion(new HashSet<>(sourceParameters), new HashSet<>(targetParameters));
        Set<String> parametersIntersection = Jaccard.getIntersection(new HashSet<>(sourceParameters), new HashSet<>(targetParameters));

        Set<String> returnUnion = Jaccard.getUnion(new HashSet<>(sourceReturn), new HashSet<>(targetReturn));
        Set<String> returnIntersection = Jaccard.getIntersection(new HashSet<>(sourceReturn), new HashSet<>(targetReturn));

        double paramCoefficent = computeEdge(parametersUnion, parametersIntersection);
        double returnCoefficient = computeEdge(returnUnion, returnIntersection);
        double coefficient = 0;

        // If there are elements for both parameters and return take both into account
        if (paramCoefficent != -1 && returnCoefficient != -1) {
            coefficient = paramCoefficent + returnCoefficient / 2;
            // If the parameters union set is empty, take only return types into account
        } else if (paramCoefficent == -1 && returnCoefficient != -1) {
            coefficient = returnCoefficient;
            // use only parameters since return union set is empty
        } else if (paramCoefficent != -1) {
            coefficient = paramCoefficent;
        }

        return coefficient;
    }


    @Override
    public double calculateService() {
        /*
            TODO:
             - Source para destino sempre
             - Fazer bilateralmente

         */
        ExtractOperations.extractAtServiceLevelInterfaces(parseResultServices, new HashSet<>(interfaces));
        ExtractOperations.mapServices(parseResultServices.getServices());
        Map<Integer, Service> services = parseResultServices.getServices();

        double chm = 0.0;
        int countedServices = 0;
        for (Service service : services.values()) {
            int sourceIndex = -1;
            double serviceJaccard = 0.0;

            Map<String, String> operationsInOrder = service.getOperations();

            if (operationsInOrder.isEmpty()) {
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
/*
                        System.out.println("[CHM Source or Target method not found] " + sourceOperation + ", " + targetOperation);
*/
                        continue;
                    }

                    double jaccard = calculateJaccardCoefficient(sourceMethod, targetMethod);
                    serviceJaccard += jaccard;

                }
            }

            // TODO: Originally stated as == 1, but how do we handle when the service has no operations?
            // We're currently skipping services with 0 operations, however, services with 0 operations should not exist at all
            if (service.getOperations().size() == 1) {
                serviceJaccard = 1;
            } else {
                int opSize = service.getOperations().size();
                serviceJaccard = opSize != 0 ? serviceJaccard / (double) (opSize * (opSize - 1) / 2) : 0;
            }


            service.setChm(serviceJaccard);

            chm += serviceJaccard;

        }

        // TODO : chm / countedServices or services.size();
        return chm / countedServices;
    }
}



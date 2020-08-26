package metrics;

import constants.Constants;
import extraction.ExtractOperations;
import graph.entities.MyClass;
import graph.entities.MyMethod;
import graph.entities.Service;
import parser.ParseResultServices;
import utils.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * Calculate the CoHesion at Message level (chd)
 * chd = 1 - (Lack of Cohesion at message level)
 * The message level refers to datatypes of parameters on method declarations and its return datatypes.
 */
public class CHD implements Metric {

    private ParseResultServices parseResultServices;
    private Set<String> interfaces;
    private boolean includeParameters;
    private boolean includeReturn;

    public CHD(ParseResultServices parseResultServices, Set<String> interfaces) {
        this.parseResultServices = parseResultServices;
        this.interfaces = interfaces;
        this.includeParameters = true;
        this.includeReturn = true;
    }

    private double calculateJaccardCoefficient(MyMethod source, MyMethod target) {
        // TODO : Consider the commented approach on RS17 (tosc-interf-dom-cohesion) on handling empty sets?
        Set<String> sourceOperationTerms = new HashSet<>(StringUtils.filterAndCleanText(source.getName(), Constants.STOP_WORDS));
        Set<String> targetOperationTerms = new HashSet<>(StringUtils.filterAndCleanText(target.getName(), Constants.STOP_WORDS));


        if (includeParameters) {
            for (String s : source.getParametersDataType()) {
                sourceOperationTerms.addAll(StringUtils.filterAndCleanText(s, Constants.STOP_WORDS));
            }
            for (String s : source.getParametersDataType()) {
                targetOperationTerms.addAll(StringUtils.filterAndCleanText(s, Constants.STOP_WORDS));
            }
        }

        if (includeReturn) {
            for (String s : StringUtils.extractVariableType(source.getVisitor().getTypeAsString())) {
                sourceOperationTerms.addAll(StringUtils.filterAndCleanText(s.toLowerCase(), Constants.STOP_WORDS));
            }

            for (String s : StringUtils.extractVariableType(target.getVisitor().getTypeAsString())) {
                targetOperationTerms.addAll(StringUtils.filterAndCleanText(s.toLowerCase(), Constants.STOP_WORDS));
            }

        }

        Set<String> union = Jaccard.getUnion(sourceOperationTerms, targetOperationTerms);
        Set<String> intersection = Jaccard.getIntersection(sourceOperationTerms, targetOperationTerms);

        if (union.size() == 0 && intersection.size() == 0)
            return 1;

        System.out.println("\tTerms:" + sourceOperationTerms.toString() + " - " + targetOperationTerms);
        System.out.println("\t" + union.toString() + " -- " + intersection.toString());
        System.out.println("\torignal: " + source.getName() + " " + source.getParametersDataType().toString() + " - "
                + target.getName() + " " + target.getParametersDataType().toString() + "\n");

        return union.isEmpty() ? 0 : intersection.size() / (double) union.size();
    }


    @Override
    public double calculateService() throws IOException {
        ExtractOperations.extractAtServiceLevelInterfaces(parseResultServices, interfaces);
        ExtractOperations.mapServices(parseResultServices.getServices());
        Map<Integer, Service> services = parseResultServices.getServices();

        Map<String, Set<String>> testFosci = new HashMap<>();
        testFosci.put("org.mybatis.jpetstore.web.actions.CatalogActionBean", new HashSet<>(Arrays.asList("viewCategory", "searchProducts", "viewProduct", "viewItem")));
        testFosci.put("org.mybatis.jpetstore.web.actions.OrderActionBean", new HashSet<>(Arrays.asList("newOrder", "isConfirmed", "getOrder",
                "newOrderForm", "clear", "setOrderId", "viewOrder", "listOrders")));
        testFosci.put("org.mybatis.jpetstore.web.actions.CartActionBean", new HashSet<>(Arrays.asList("clear", "removeItemFromCart", "updateCartQuantities", "getCart",
                "addItemToCart")));
        testFosci.put("org.mybatis.jpetstore.web.actions.AccountActionBean", new HashSet<>(Arrays.asList("isAuthenticated", "getUsername", "setPassword", "setUsername",
                "newAccount", "getAccount", "signoff", "clear")));


        for(Service service: services.values()){
            for(MyClass my : service.getClasses().values()){
                if(testFosci.containsKey(my.getQualifiedName())){
                    my.setOperations(testFosci.get(my.getQualifiedName()));
                }

            }
        }


        double chd = 0.0;
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
            if (service.getOperations().size() == 1) {
                serviceJaccard = 1;
            } else {
                int opSize = service.getOperations().size();
                serviceJaccard = opSize != 0 ? serviceJaccard / (double) (opSize * (opSize - 1) / 2) : 0;
            }

            service.setChd(serviceJaccard);
            System.out.println(service.getId() + " CHD: " + serviceJaccard);
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



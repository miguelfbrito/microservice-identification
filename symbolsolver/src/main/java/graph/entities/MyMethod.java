package graph.entities;

import java.util.ArrayList;
import java.util.List;

public class MyMethod {
    private String name;
    private List<String> parametersDataType;
    private List<String> returnDataType;

    public MyMethod(String name) {
        this.name = name;
        this.parametersDataType = new ArrayList<>();
    }

    public MyMethod(String name, List<String> parametersDataType, String returnDataType) {
        this.name = name;
        this.parametersDataType = parametersDataType;
        this.returnDataType.add(returnDataType);
    }

    public MyMethod(String name, List<String> parametersDataType, List<String> returnDataType) {
        this.name = name;
        this.parametersDataType = parametersDataType;
        this.returnDataType = returnDataType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getParametersDataType() {
        return parametersDataType;
    }

    public void setParametersDataType(List<String> parametersDataType) {
        this.parametersDataType = parametersDataType;
    }

    public List<String> getReturnDataType() {
        return returnDataType;
    }

    public void setReturnDataType(List<String> returnDataType) {
        this.returnDataType = returnDataType;
    }

    @Override
    public String toString() {
        return "MyMethod{" +
                "parametersDataType=" + parametersDataType +
                ", returnDataType='" + returnDataType + '\'' +
                '}';
    }
}

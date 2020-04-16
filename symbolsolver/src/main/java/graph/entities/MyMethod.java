package graph.entities;

import java.util.ArrayList;
import java.util.List;

public class MyMethod {
    private String name;
    private List<String> parametersDataType;
    private String returnDataType;

    public MyMethod(String name) {
        this.name = name;
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

    public String getReturnDataType() {
        return returnDataType;
    }

    public void setReturnDataType(String returnDataType) {
        this.returnDataType = returnDataType;
    }
}

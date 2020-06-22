package graph.entities;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.gson.annotations.Expose;
import constants.Constants;

import java.util.*;
import java.util.stream.Collectors;


public class MyMethod {
    private MyClass myClass;
    @Expose
    private String name;
    @Expose
    private List<String> parametersDataType;
    @Expose
    private List<String> returnDataType;
    private MethodDeclaration visitor;

    // TODO : Add myClass as parent on constructors
    public MyMethod(String name) {
        this.name = name;
        this.parametersDataType = new ArrayList<>();
    }

    public MyMethod(String name, List<String> parametersDataType, String returnDataType) {
        this.name = name;
        this.setParametersDataType(parametersDataType);
        this.setReturnDataType(Collections.singletonList(returnDataType));
    }

    public MyMethod(String name, List<String> parametersDataType, List<String> returnDataType) {
        this.name = name;
        this.setParametersDataType(parametersDataType);
        this.setReturnDataType(returnDataType);
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
        this.parametersDataType = parametersDataType.stream().filter(s -> !Constants.STOP_WORDS_DATA_TYPES.contains(s.toLowerCase())).collect(Collectors.toList());
    }

    public List<String> getReturnDataType() {
        return returnDataType;
    }

    public void setReturnDataType(List<String> returnDataType) {
        this.returnDataType = returnDataType.stream().filter(s -> !Constants.STOP_WORDS_DATA_TYPES.contains(s.toLowerCase())).collect(Collectors.toList());
    }

    public MyClass getMyClass() {
        return myClass;
    }

    public void setVisitor(MethodDeclaration visitor) {
        this.visitor = visitor;
    }

    public MethodDeclaration getVisitor() {
        return visitor;
    }

    public void setMyClass(MyClass myClass) {
        if (myClass.getMethods().containsKey(name)) {
            this.myClass = myClass;
        }
    }

    @Override
    public String toString() {
        return "MyMethod{" +
                "parametersDataType=" + parametersDataType +
                ", returnDataType='" + returnDataType + '\'' +
                '}';
    }
}

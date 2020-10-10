package graph.entities;

import com.google.gson.annotations.Expose;

public class MethodInvocationDTO {

    @Expose
    private String scopeName;
    @Expose
    private String methodName;
    @Expose
    private String targetClassName;

    public MethodInvocationDTO(String scopeName, String methodName, String targetClassName) {
        this.scopeName = scopeName;
        this.methodName = methodName;
        this.targetClassName = targetClassName;
    }

    public String getScopeName() {
        return scopeName;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getTargetClassName() {
        return targetClassName;
    }
}

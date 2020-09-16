package stats;

import com.google.gson.annotations.Expose;

public class ClassStats {
    @Expose
    private int methodDeclarations;

    @Expose
    private int methodInvocations;


    public ClassStats(int methodDeclarations, int methodInvocations) {
        this.methodDeclarations = methodDeclarations;
        this.methodInvocations = methodInvocations;
    }

    public int getMethodDeclarations() {
        return methodDeclarations;
    }

    public int getMethodInvocations() {
        return methodInvocations;
    }
}

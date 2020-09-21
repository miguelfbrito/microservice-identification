package stats;

import com.google.gson.annotations.Expose;

public class ClassStats {
    @Expose
    private int methodDeclarations;

    @Expose
    private int methodInvocations;

    @Expose
    private int totalDependencies;

    @Expose
    private int totalUniqueDependencies;

    public ClassStats(int methodDeclarations, int methodInvocations, int totalDependencies, int totalUniqueDependencies) {
        this.methodDeclarations = methodDeclarations;
        this.methodInvocations = methodInvocations;
        this.totalDependencies = totalDependencies;
        this.totalUniqueDependencies = totalUniqueDependencies;
    }

    public int getTotalDependencies() {
        return totalDependencies;
    }

    public int getTotalUniqueDependencies() {
        return totalUniqueDependencies;
    }

    public int getMethodDeclarations() {
        return methodDeclarations;
    }

    public int getMethodInvocations() {
        return methodInvocations;
    }


}

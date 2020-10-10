package stats;

import com.google.gson.annotations.Expose;

import java.util.List;

public class ClassStats {
    @Expose
    private int methodDeclarations;

    @Expose
    private List<String> methodInvocations;

    @Expose
    private int totalDependencies;

    @Expose
    private int totalUniqueDependencies;

    public ClassStats(int methodDeclarations, List<String> methodInvocations, int totalDependencies, int totalUniqueDependencies) {
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

    public List<String> getMethodInvocations() {
        return methodInvocations;
    }


}

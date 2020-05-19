package projects;

public class Project {

    private String id;
    private String name;
    private String rootPath;
    private String relativePath;
    private String clusterString;
    private String commitHash;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getClusterString() {
        return clusterString;
    }

    public void setClusterString(String clusterString) {
        this.clusterString = clusterString;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public String getCommitHash() {
        return commitHash;
    }

    public void setCommitHash(String commitHash) {
        this.commitHash = commitHash;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Project{" +
                "name='" + name + '\'' +
                ", rootPath='" + rootPath + '\'' +
                ", relativePath='" + relativePath + '\'' +
                ", clusterString='" + clusterString + '\'' +
                ", commitHash='" + commitHash + '\'' +
                '}';
    }
}

package projects;

public class Project {

    private String name;
    private String rootPath;
    private String path;
    private String clusterString;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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

    @Override
    public String toString() {
        return "Project{" +
                "name='" + name + '\'' +
                ", rootPath='" + rootPath + '\'' +
                ", path='" + path + '\'' +
                ", clusterString='" + clusterString + '\'' +
                '}';
    }
}

package projects;

public class ProjectMetrics {

    private Project project;
    private double irn;
    private double opn;
    private double chm;
    private double chd;

    public ProjectMetrics(Project project) {
        this.project = project;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public double getIrn() {
        return irn;
    }

    public void setIrn(double irn) {
        this.irn = irn;
    }

    public double getOpn() {
        return opn;
    }

    public void setOpn(double opn) {
        this.opn = opn;
    }

    public double getChm() {
        return chm;
    }

    public void setChm(double chm) {
        this.chm = chm;
    }

    public double getChd() {
        return chd;
    }

    public void setChd(double chd) {
        this.chd = chd;
    }

    @Override
    public String toString() {
        return "ProjectMetrics{" +
                "project=" + project.getName()+
                ", irn=" + irn +
                ", opn=" + opn +
                ", chm=" + chm +
                ", chd=" + chd +
                '}';
    }
}

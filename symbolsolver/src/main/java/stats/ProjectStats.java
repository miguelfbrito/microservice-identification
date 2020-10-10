package stats;

import com.google.gson.annotations.Expose;
import stats.ClassStats;

import java.util.Map;

public class ProjectStats {

    @Expose
    private Map<String, ClassStats> classes;

    @Expose
    private int totalClasses;

    public ProjectStats(Map<String, ClassStats> classes) {
        this.classes = classes;
        this.totalClasses = classes.keySet().size();
    }

    public Map<String, ClassStats> getClasses() {
        return classes;
    }

    public int getTotalClasses() {
        return totalClasses;
    }
}


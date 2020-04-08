package graph;

import org.jgrapht.graph.DefaultEdge;

public class DependencyEdge extends DefaultEdge {
    private String label;

    public DependencyEdge(String label){
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
    
    @Override
    public String toString()
    {
        return "(" + getSource() + " : " + getTarget() + " : " + label + ")";
    }

}

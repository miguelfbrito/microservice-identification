package graph;

import org.jgrapht.graph.DefaultEdge;

public class DependencyEdge extends DefaultEdge {
    private String label;
    private double value;

    public DependencyEdge(String label){
        this.label = label;
        this.value = 1;
    }

    public DependencyEdge(String label, double value){
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public String toString()
    {
        return "(" + getSource() + " : " + getTarget() + " : " + label + " : " + value + " )";
    }


}

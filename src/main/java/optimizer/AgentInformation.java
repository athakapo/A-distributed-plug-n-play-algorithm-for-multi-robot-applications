package optimizer;

import org.ejml.simple.SimpleMatrix;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by atkap on 1/13/2017.
 */
public class AgentInformation {

    private List<Double> costFunctionHistory = new LinkedList<Double>();
    private List<double[]> phistory = new LinkedList<double[]>();
    private int d;
    private double[] currentState;
    private SimpleMatrix theta;
    private HashSet<String> fringe;
    private ArrayList<Integer[][]> preservedFeatures;
    private ArrayList<Integer[][]> usedFeatures;
    //PreservedFeatures fringe

    AgentInformation(int d) {
        this.d = d;
        this.currentState = new double[d];
        this.fringe = new HashSet<String>();
        this.preservedFeatures = new ArrayList<Integer[][]>();
        this.usedFeatures = new ArrayList<Integer[][]>();
    }

    public void setusedFeatures(ArrayList<Integer[][]> UF) {
        this.usedFeatures = UF;
    }

    public ArrayList<Integer[][]> getusedFeatures() {
        return usedFeatures;
    }

    public void setPreservedFeatures(ArrayList<Integer[][]> pF) {
        this.preservedFeatures = pF;
    }

    public ArrayList<Integer[][]> getPreservedFeatures() {
        return preservedFeatures;
    }

    public void setFringe(HashSet<String> f) {
        this.fringe = f;
    }

    public HashSet<String> getFringe() {
        return fringe;
    }

    public void setTheta(SimpleMatrix M) {
        this.theta = new SimpleMatrix(M);
    }

    public SimpleMatrix getTheta() {
        return this.theta;
    }

    public double[] getCurrentState() {
        return currentState;
    }

    public void setCurrentState(double[] v) {
        for (int j = 0; j < d; j++) {
            currentState[j] = v[j];
        }
    }

    public List<Double> getCostFunctionHistory() {
        return costFunctionHistory;
    }

    public void setCostFunctionHistory(double v) {
        costFunctionHistory.add(v);
    }

    public void removeFromStartSetCostFunctionHistory(double v) {
        costFunctionHistory.remove(0);
        costFunctionHistory.add(v);
    }

    public List<double[]> getPhistory() {
        return phistory;
    }

    public void setPhistory(double[] v) {
        phistory.add(v);
    }

    public void removeFromStartSetPhistory(double[] v) {
        phistory.remove(0);
        phistory.add(v);
    }

}

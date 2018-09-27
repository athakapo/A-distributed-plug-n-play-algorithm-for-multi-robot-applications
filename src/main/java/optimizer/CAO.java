package optimizer;

import environment.Setup;
import org.ejml.simple.SimpleMatrix;

import java.util.*;

/**
 * Created by atkap on 1/14/2017.
 */
public class CAO {

    private AgentInformation[] AgentsInf;
    private int d, noPerturbations, noMonomials, monoMaxOrder, n_ss;
    private Random rnd;
    private Setup prob;
    private double perMono;
    private int[] monomPerOrder;
    private int[] preservedMonomials;


    CAO(AgentInformation[] ag, int D, int NoPert, int NoMonomials, int NoMaxMon, double perMono, Setup prob) {
        this.AgentsInf = ag;
        this.d = D;
        this.noPerturbations = NoPert;
        this.rnd = new Random();
        this.noMonomials = NoMonomials;
        this.monoMaxOrder = NoMaxMon;
        this.n_ss = D;
        this.prob = prob;
        this.perMono = perMono;

        CalculateMonomialsPerOrder();
    }

    private void CalculateMonomialsPerOrder() {
        //TODO automatically
        for (int i = 0; i < monoMaxOrder; i++) {

        }
        monomPerOrder = new int[]{2, 3, 4};  //TODO fix me
        preservedMonomials = new int[]{2, 3, 4};  //TODO fix me

    }

    protected double[] FindBestNewActions(int r, double CurrentPetrub) {
        double[] finalAction = new double[d];
        AgentInformation CurRobot = AgentsInf[r];

        polynomialEstimator(CurRobot);

        double[] perturbBase = CurRobot.getCurrentState();

        double BestEsti = Double.MAX_VALUE;
        double currEsti;

        int iter = 0;
        while (iter <= noPerturbations) {
            double[] cand = new double[d];
            if (iter == 0) {
                for (int j = 0; j < d; j++) {
                    cand[j] = perturbBase[j];
                }
            } else {

                for (int j = 0; j < d; j++) {
                    cand[j] = perturbBase[j] + CurrentPetrub * (2 * rnd.nextDouble() - 1);
                }
            }
            if (prob.isThisAValidDecisionCommand(r, cand)) {
                currEsti = calculateEstimation(CurRobot, cand);
                if (currEsti < BestEsti) {
                    BestEsti = currEsti;
                    System.arraycopy(cand, 0, finalAction, 0, d);
                }
                iter++;
            }
        }
        return finalAction;
    }


    private void polynomialEstimator(AgentInformation system) {

        CalculateCurrentFeatures(system);

        int m = system.getCostFunctionHistory().size();
        SimpleMatrix phi = new SimpleMatrix(new double[noMonomials + 1][m]);


        List<double[]> SystemM = system.getPhistory();

        double[][] CostTable = new double[m][1];
        for (int i = 0; i < m; i++) {
            CostTable[i][0] = system.getCostFunctionHistory().get(i);
        }


        for (int j = 0; j < m; j++) {
            int q = 0;
            for (int k = 0; k < monoMaxOrder; k++) {
                Integer[][] Features = system.getusedFeatures().get(k);
                int diffInst = Features[0].length;
                for (int ii = 0; ii < diffInst; ii++) {
                    phi.set(q, j, SystemM.get(j)[Features[0][ii]]);
                    for (int i = 1; i <= k; i++) {
                        phi.set(q, j, phi.get(q, j) * SystemM.get(j)[Features[i][ii]]);
                    }
                    q++;
                }
            }
            phi.set(q, j, 1.0);
        }

        //For readability --> Theta=pinv(phi')*CostTable;
        // Property pinv(phi')=pinv(phi)'
        //system.setTheta(phi.inverse().transpose().times(new Matrix(CostTable)));
        try {
            system.setTheta(phi.transpose().pseudoInverse().mult(new SimpleMatrix(CostTable)));
        } catch (Exception e) {
            System.out.println("gamw tous pinakes");
        }

        CalculateDominantFeatures(system);

    }


    private void CalculateCurrentFeatures(AgentInformation system) {
        Random locRnd = new Random();
        ArrayList<Integer[][]> usedFeatures = new ArrayList<>();

        int NoPrev, NoFet;
        HashSet<String> R_fringe = system.getFringe();
        for (int i = 0; i < monoMaxOrder; i++) {
            NoPrev = 0;
            if (system.getPreservedFeatures().size() > 0) {
                NoPrev = system.getPreservedFeatures().get(i)[0].length;
            }
            NoFet = monomPerOrder[i] - NoPrev;
            int j = 0;
            Integer[][] newFetToADD = new Integer[i + 1][NoFet];
            while (j < NoFet) {
                ArrayList<Integer> lfet = new ArrayList<>();
                for (int k = 0; k < i + 1; k++) {
                    lfet.add(locRnd.nextInt(n_ss));
                }
                Collections.sort(lfet);
                String lkey = lfet.toString();
                if (!R_fringe.contains(lkey)) {
                    R_fringe.add(lkey);
                    for (int k = 0; k < i + 1; k++) {
                        newFetToADD[k][j] = lfet.get(k);
                    }
                    j++;
                }
            }

            int cols = NoPrev + NoFet;
            Integer[][] finalFeat = new Integer[i + 1][cols];
            for (int q = 0; q < i + 1; q++) {
                for (int k = 0; k < NoPrev; k++) {
                    finalFeat[q][k] = system.getPreservedFeatures().get(i)[q][k];
                }
            }
            for (int q = 0; q < i + 1; q++) {
                int k1 = 0;
                for (int k = NoPrev; k < cols; k++) {
                    finalFeat[q][k] = newFetToADD[q][k1];
                    k1++;
                }
            }
            usedFeatures.add(finalFeat);
        }

        system.setusedFeatures(usedFeatures);
    }

    private void CalculateDominantFeatures(AgentInformation system) {
        HashSet<String> R_fringe = new HashSet<>();
        ArrayList<Integer[][]> dominantFeatures = new ArrayList<>();

        int indxS = 0;
        int indxE, NextValuableFeature;
        for (int i = 0; i < monoMaxOrder; i++) {
            indxE = indxS + system.getusedFeatures().get(i)[0].length - 1;
            //Matrix ThetaIOrder = system.getTheta().getMatrix(indxS,indxE,0,0);
            if (indxE - indxS + 1 > preservedMonomials[i]) {
                Pair[] pairTheta = new Pair[indxE - indxS];
                for (int j = 0; j < indxE - indxS; j++) {
                    double thetaElem = system.getTheta().get(indxS + j, 0);
                    if (thetaElem >= 0) {
                        pairTheta[j] = new Pair(j, thetaElem);
                    } else {
                        pairTheta[j] = new Pair(j, -thetaElem);
                    }
                }
                Arrays.sort(pairTheta);
                Integer[][] currFeature = new Integer[i + 1][preservedMonomials[i]];
                for (int j = 0; j < preservedMonomials[i]; j++) {
                    NextValuableFeature = pairTheta[j].index;
                    ArrayList<Integer> tempkey = new ArrayList<>();
                    for (int k = 0; k < i + 1; k++) {
                        int vi = system.getusedFeatures().get(i)[k][NextValuableFeature];
                        currFeature[k][j] = vi;
                        tempkey.add(vi);
                    }
                    //Collections.sort(tempkey);
                    R_fringe.add(tempkey.toString());
                }
                dominantFeatures.add(currFeature);
            } else {
                dominantFeatures.add(system.getusedFeatures().get(i));
            }
            indxS = indxE + 1;
        }

        system.setFringe(R_fringe);
        system.setPreservedFeatures(dominantFeatures);
    }

    private double calculateEstimation(AgentInformation system, double[] cand) {
        SimpleMatrix PHI = new SimpleMatrix(new double[noMonomials + 1][1]);
        int q = 0;
        for (int k = 0; k < monoMaxOrder; k++) {
            Integer[][] Features = system.getusedFeatures().get(k);
            int diffInst = Features[0].length;
            for (int ii = 0; ii < diffInst; ii++) {
                PHI.set(q, 0, 1.0);
                for (int i = 0; i <= k; i++) {
                    PHI.set(q, 0, PHI.get(q, 0) * cand[Features[i][ii]]);
                }
                q++;
            }
        }
        PHI.set(q, 0, 1.0);
        return system.getTheta().transpose().mult(PHI).get(0, 0);
    }

    private class Pair implements Comparable<Pair> {
        private final int index;
        private final double value;

        public Pair(int index, double value) {
            this.index = index;
            this.value = value;
        }

        @Override
        public int compareTo(Pair other) {
            //multiplied to -1 as the author need descending sort order
            return -1 * Double.valueOf(this.value).compareTo(other.value);
        }
    }


}

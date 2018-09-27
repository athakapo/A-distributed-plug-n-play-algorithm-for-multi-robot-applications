package optimizer;

import environment.Setup;

import java.util.Random;

/**
 * Created by atkap on 1/12/2017.
 */
public class CentralizedDecisionMaking {
    private int n, D, fw;
    private double dt, jprevI, ja;
    private Random rnd;
    private double[][] pold;
    private Setup testbed;
    private AgentInformation[] AllAgents;
    private CAO optimizer;
    private double[] petrubConst;

    public CentralizedDecisionMaking(int nr, int D, double pert, double[][] initP, int fw, int Tmax,
                                     int NoPert, int monomials, int maxOrder, double pertrubUpBound, double pertrubLowBound,
                                     double perMono, Setup testbed) {
        this.n = nr;
        this.D = D;
        this.dt = pert;
        this.rnd = new Random();
        this.pold = deepCPmatrix(initP);
        this.fw = fw;
        this.testbed = testbed;

        petrubConst = new double[Tmax];
        double UpBound = pertrubUpBound * dt;
        double LowBound = pertrubLowBound * dt;

        petrubConst[0] = UpBound;
        for (int i = 1; i < Tmax; i++) {
            petrubConst[i] = petrubConst[i - 1] - (UpBound - LowBound) / Tmax;
        }

        AllAgents = new AgentInformation[n];
        for (int i = 0; i < n; i++) {
            AllAgents[i] = new AgentInformation(this.D);
            AllAgents[i].setCurrentState(initP[i]);
        }
        this.optimizer = new CAO(AllAgents, this.D, NoPert, monomials, maxOrder, perMono, testbed);

    }


    public double[][] produceDecisionVariables(int t, double[][] p, double J) {
        double[][] hp = deepCPmatrix(p);
        if (t <= fw) {
            for (int i = 0; i < n; i++) {
                double[][] ptemp = deepCPmatrix(p);
                AllAgents[i].setPhistory(hp[i]);
                for (int j = 0; j < D; j++) {
                    ptemp[i][j] = pold[i][j];
                }
                jprevI = testbed.EvaluateCF(ptemp, i);
                if (t == 0) ja = 2 * J - jprevI;
                else ja = AllAgents[i].getCostFunctionHistory().get(t - 1) + J - jprevI;
                AllAgents[i].setCostFunctionHistory(ja);
            }
        } else {
            for (int i = 0; i < n; i++) {
                double[][] ptemp = deepCPmatrix(p);
                AllAgents[i].removeFromStartSetPhistory(hp[i]);
                for (int j = 0; j < D; j++) {
                    ptemp[i][j] = pold[i][j];
                }
                jprevI = testbed.EvaluateCF(ptemp, i);
                ja = AllAgents[i].getCostFunctionHistory().get(fw) + J - jprevI;
                AllAgents[i].removeFromStartSetCostFunctionHistory(ja);
            }
        }

        pold = deepCPmatrix(p);
        double[][] pnew = new double[n][D];
        for (int i = 0; i < n; i++) {
            pnew[i] = optimizer.FindBestNewActions(i, petrubConst[t]);
            AllAgents[i].setCurrentState(pnew[i]);
        }

        return pnew;
    }

    private double[][] deepCPmatrix(double[][] a) {
        int rows = a.length;
        int cols = a[0].length;
        double[][] b = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                b[i][j] = a[i][j];
            }
        }
        return b;
    }

    public double getPetrubConst(int t) {
        return petrubConst[t];
    }
}

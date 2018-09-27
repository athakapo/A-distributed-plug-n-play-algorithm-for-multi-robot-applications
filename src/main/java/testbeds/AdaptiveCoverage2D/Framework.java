package testbeds.AdaptiveCoverage2D;

import environment.Setup;
import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.ejml.simple.SimpleMatrix;

import java.util.ArrayList;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;


public class Framework extends Setup {

    private double[][] Q, gaussianC, sigma;
    private int N, m, sm, sl;
    private final double sigmaj = 0.02;
    private final double amin = 0.1;
    private final double g = 0.01;
    private final double z = 0.005;
    private final double maxD = 1.0;
    private final double minD = 0.0;


    private SimpleMatrix Kappa, Gamma, w, lamdaC, lC, hatAC, RealA, C, Lap;
    private ArrayList<SimpleMatrix> Lamda, l, AllCalKappa, hatA, Fval, LapPerRobot;

    public Framework() {
    }

    public void worldConstructor() {
        //Number of points constituting the world [Resolution]
        N = 225; //225,529 (It should be a perfect square)
        sl = (int) sqrt(N);

        //2D Representation of the world
        Q = equalSeparation(sl);

        //Continuous basis functions {\cal K}
        //Covariances on gaussians
        sigma = new double[][]{{sigmaj, 0}, {0, sigmaj}};

        //Number of gaussians in the same row
        sm = 7;
        m = (int) pow(sm, 2);
        gaussianC = equalSeparation(sm);


        //Estimation Parameters
        hatA = new ArrayList<>();
        for (int i = 0; i < nr; i++) {
            SimpleMatrix hatAr = InitializeSimpleMatrix(m, 1, amin);
            hatA.add(hatAr);
        }
        Lamda = new ArrayList<>();
        l = new ArrayList<>();
        for (int i = 0; i < nr; i++) {
            Lamda.add(new SimpleMatrix(m, m));
            l.add(new SimpleMatrix(m, 1));
        }

        //Gains
        Kappa = SimpleMatrix.identity(2).scale(3.0);
        Gamma = SimpleMatrix.identity(m);

        //Centralized parameters
        w = InitializeSimpleMatrix(nr, 1, 1.0);
        lamdaC = new SimpleMatrix(m, m);
        lC = new SimpleMatrix(m, 1);
        hatAC = InitializeSimpleMatrix(m, 1, amin);


        //AllCalKappa
        AllCalKappa = new ArrayList<>();
        for (int i = 0; i < N; i++) { //for each point
            AllCalKappa.add(CalKappa(Q[i]));
        }

        //True mixing parameters
        RealA = InitializeSimpleMatrix(m, 1, amin);
        RealA.set(0, 0, 100.0);
        RealA.set(15, 0, 100.0);
    }

    public double CalculateCF(double[][] p) {
        //Calculate Voronoi Partitions
        int[] V = VoronoiPartitions(true, p);

        //Calculate robots centroids and the Cost function value
        double J = CentroidApproximation(true, V, p);

        //Update the estimation of the mixing vector alpha
        UpdateHatA();

        return J;
    }

    public double EvaluateCF(double[][] p, int r) {
        //Evaluate CF without updating any of the class attributes
        return CentroidApproximation(false, VoronoiPartitions(false, p), p);
    }

    public boolean isThisAValidDecisionCommand(int i, double[] p) {
        for (int j = 0; j < D; j++) {
            if (p[j] > maxD || p[j] < minD)
                return false;
        }
        return true;
    }

    public void fetchDecisionVector() {
        // Set the robots initial positions manually
        // randomID field in parameters file should be false
        setInitialDecisionVector(new double[][]{
                {0.748782920935759, 0.523484207016743},
                {0.521725763522798, 0.662285767439358},
                {0.618314982927528, 0.850496608951951},
                {0.987035204212623, 0.683118335623724},
                {0.560976459754866, 0.480022865952500},
                {0.796815587856705, 0.712148754079348},
                {0.904113237958921, 0.006839213657844},
                {0.687208306090933, 0.641243548188644},
                {0.822574509070901, 0.141788922472766},
                {0.863995313984828, 0.247451873545336}});
    }

    private int[] VoronoiPartitions(boolean updateParam, double[][] p) {
        int[] V = new int[N];

        for (int q = 0; q < N; q++) {
            double disVec;
            int imin = 0;
            double minV = norm(Q[q], p[0]);
            for (int r = 1; r < nr; r++) {
                disVec = norm(Q[q], p[r]);
                if (disVec < minV) {
                    minV = disVec;
                    imin = r;
                }
            }
            V[q] = imin;
        }


        if (updateParam) {
            int[][] A = reshape(V, sl, sl);
            SimpleMatrix localLap = SimpleMatrix.identity(nr);

            for (int i = 0; i < sl; i++) {
                for (int j = 0; j < sl; j++) {
                    if ((i + 1 < sl) && (j - 1 >= 0)) {
                        if (A[i + 1][j - 1] != A[i][j]) {
                            localLap.set(A[i + 1][j - 1], A[i][j], 1.0);
                            localLap.set(A[i][j], A[i + 1][j - 1], 1.0);
                        }
                    }
                    if (i + 1 < sl) {
                        if (A[i + 1][j] != A[i][j]) {
                            localLap.set(A[i + 1][j], A[i][j], 1.0);
                            localLap.set(A[i][j], A[i + 1][j], 1.0);
                        }
                    }
                    if ((i + 1 < sl) && (j + 1 < sl)) {
                        if (A[i + 1][j + 1] != A[i][j]) {
                            localLap.set(A[i + 1][j + 1], A[i][j], 1.0);
                            localLap.set(A[i][j], A[i + 1][j + 1], 1.0);
                        }
                    }
                    if (j + 1 < sl) {
                        if (A[i][j + 1] != A[i][j]) {
                            localLap.set(A[i][j + 1], A[i][j], 1.0);
                            localLap.set(A[i][j], A[i][j + 1], 1.0);
                        }
                    }
                }
            }

            Lap = localLap;
            LapPerRobot = new ArrayList<>();
            for (int i = 0; i < nr; i++) {
                SimpleMatrix LapR = new SimpleMatrix(1, nr);
                for (int r = 0; r < nr; r++) {
                    LapR.set(0, r, Lap.get(i, r));
                }
                LapPerRobot.add(LapR);
            }
        }

        return V;
    }

    //Mexri edw yparxei theos

    private double CentroidApproximation(boolean updateParam, int[] V, double[][] p) {
        /**
         * Implements equation (7) from:
         *
         * Schwager, Mac, Daniela Rus, and Jean-Jacques Slotine.
         * "Decentralized, adaptive coverage control for networked robots."
         * The International Journal of Robotics Research 28.3 (2009): 357-375.
         */
        //Calculate M hat
        ArrayList<SimpleMatrix> Fvallocal = new ArrayList<>();
        SimpleMatrix Clocal = new SimpleMatrix(nr, 2);
        double H = 0.0;

        for (int r = 0; r < nr; r++) {

            ArrayList<double[]> Si = new ArrayList<>();
            ArrayList<SimpleMatrix> calKappaList = new ArrayList<>();
            for (int j = 0; j < N; j++) {
                if (V[j] == r) {
                    Si.add(Q[j]);
                    calKappaList.add(AllCalKappa.get(j));
                }
            }
            int li = Si.size();
            double M = 0.0;
            SimpleMatrix L = new SimpleMatrix(1, 2);
            SimpleMatrix F1 = new SimpleMatrix(m, 2);
            SimpleMatrix F2 = new SimpleMatrix(2, m);
            SimpleMatrix AllPhi = new SimpleMatrix(li, 1);


            for (int i = 0; i < li; i++) {
                double[] q = Si.get(i);
                SimpleMatrix calKappa = calKappaList.get(i);
                double hatPhi = calKappa.transpose().mult(hatAC).get(0);
                double Phi = calKappa.transpose().mult(RealA).get(0);
                AllPhi.set(i, 0, Phi);
                SimpleMatrix point2robot = new SimpleMatrix(new double[][]{{q[0] - p[r][0], q[1] - p[r][1]}});
                F1 = F1.plus(calKappa.mult(point2robot));
                F2 = F2.plus(point2robot.transpose().mult(calKappa.transpose()));
                M += hatPhi;
                L = L.plus(new SimpleMatrix(new double[][]{q}).scale(hatPhi));
                H += point2robot.normF() * hatPhi;
            }

            Fvallocal.add(F1.mult(Kappa).mult(F2).scale(1.0 / M));
            Clocal.setRow(r, 0, L.get(0) / M, L.get(1) / M);


        }

        if (updateParam) {
            Fval = Fvallocal;
            C = Clocal;
        }

        return H / 2.0;
    }


    private void UpdateHatA() {
        /**
         * Implements equations (14) and (11) from:
         *
         * Schwager, Mac, Daniela Rus, and Jean-Jacques Slotine.
         * "Decentralized, adaptive coverage control for networked robots."
         * The International Journal of Robotics Research 28.3 (2009): 357-375.
         */
        SimpleMatrix w = InitializeSimpleMatrix(nr, 1, 1.0);
        ArrayList<SimpleMatrix> hatAT = new ArrayList<>();
        for (int i = 0; i < nr; i++) {
            hatAT.add(new SimpleMatrix(m, 1));
        }

        SimpleMatrix Iproj, calcKapppa, lamdaRobot, lRobot, cons, dotApre, dotApreC;
        for (int r = 0; r < nr; r++) {
            calcKapppa = CalKappa(getLatestDecisionVariables()[r]);

            lamdaRobot = (calcKapppa.mult(calcKapppa.transpose())).scale(w.get(r, 0) * dt);
            lRobot = calcKapppa.mult(calcKapppa.transpose().mult(RealA)).scale(w.get(r, 0) * dt);

            lamdaC = lamdaC.plus(lamdaRobot);
            lC = lC.plus(lRobot);

            Lamda.set(r, Lamda.get(r).plus(lamdaRobot));
            l.set(r, l.get(r).plus(lRobot));

            cons = new SimpleMatrix(m, 1);
            if (z != 0) {
                w = countPerRow(InitializeSimpleMatrix(nr, 1, 1.0).mult(LapPerRobot.get(r)).plus(Lap), 2.0);
                for (int j = 0; j < nr; j++) {
                    if (r != j) {
                        cons = cons.plus((hatA.get(r).minus(hatA.get(j))).scale(w.get(j, 0)));
                    }
                }
            }

            dotApre = (Fval.get(r).mult(hatA.get(r)).scale(-1.0)).minus(((Lamda.get(r).mult(hatA.get(r))).minus(l.get(r))).scale(g))
                    .minus(cons.scale(z));

            hatAT.set(r, hatA.get(r).plus(Gamma.mult(dotApre).scale(dt)));
            Iproj = CalIproj(hatAT.get(r), dotApre);
            hatA.set(r, hatA.get(r).plus(Gamma.scale(dt).mult((dotApre.minus(Iproj.mult(dotApre))))));
        }

        dotApreC = ((lamdaC.mult(hatAC)).minus(lC)).scale(-g);
        Iproj = CalIproj(hatAC.plus(Gamma.mult(dotApreC).scale(dt)), dotApreC);
        hatAC = hatAC.plus(Gamma.scale(dt).mult(dotApreC.minus(Iproj.mult(dotApreC))));

    }


    private double norm(double[] A, double[] B) {
        int l = A.length;
        double val = 0.0;
        for (int i = 0; i < l; i++)
            val += pow(A[i] - B[i], 2);
        return pow(val, 0.5);
    }

    private int[][] reshape(int[] A, int a, int b) {
        int[][] B = new int[a][b];
        int k = 0;
        for (int i = 0; i < a; i++) {
            for (int j = 0; j < b; j++) {
                B[i][j] = A[k];
                k++;
            }
        }
        return B;
    }


    private double[][] equalSeparation(int q) {
        int k = 0;
        double[][] A = new double[(int) pow(q, 2)][2];
        for (int i = 1; i <= q; i++) {
            for (int j = 1; j <= q; j++) {
                A[k][0] = i / (q + 1.0);
                A[k][1] = j / (q + 1.0);
                k++;
            }
        }
        return A;
    }

    private SimpleMatrix CalKappa(double[] q) {
        SimpleMatrix Kap = new SimpleMatrix(m, 1);
        for (int i = 0; i < m; i++) {
            Kap.set(i, 0, (new MultivariateNormalDistribution(gaussianC[i], sigma)).density(q));
        }
        return Kap;
    }

    private SimpleMatrix InitializeSimpleMatrix(int rows, int cols, double value) {
        SimpleMatrix A = new SimpleMatrix(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                A.set(i, j, value);
            }
        }
        return A;
    }

    private SimpleMatrix countPerRow(SimpleMatrix A, double num) {
        SimpleMatrix returnM = new SimpleMatrix(A.numRows(), 1);
        for (int i = 0; i < A.numRows(); i++) {
            int count = 0;
            for (int j = 0; j < A.numCols(); j++) {
                if (A.get(i, j) == num) {
                    count++;
                }
            }
            returnM.set(i, 0, count);
        }
        return returnM;
    }

    private SimpleMatrix CalIproj(SimpleMatrix ha, SimpleMatrix dotha) {
        SimpleMatrix Iproj = SimpleMatrix.identity(m);
        for (int i = 0; i < m; i++) {
            if ((ha.get(i, 0) > amin) || (ha.get(i, 0) == amin) && (dotha.get(i, 0) >= 0)) {
                Iproj.set(i, i, 0.0);
            }
        }
        return Iproj;
    }

}

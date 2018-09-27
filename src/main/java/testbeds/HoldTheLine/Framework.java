package testbeds.HoldTheLine;

import environment.Setup;
import matlabFilesBuilder.WriteLogFiles;

public class Framework extends Setup {


    //private static final double xTarget = 0.8;
    private double[][] pointsToMonitor;
    private int kw, n_p;
    private WriteLogFiles logHelper;
    private final double maxD = 1.0;
    private final double minD = 0.0;
    private final double thres = 0.0;


    public Framework() {
    }

    public void worldConstructor() {
        n_p = 2000;
        kw = n_p / 25;
        pointsToMonitor = new double[n_p][2];

        /* Straight border-line
        double yTarget = 0.0;
        for(int i=0;i<n_p;i++){
            pointsToMonitor[i][0] = xTarget;
            pointsToMonitor[i][1] = yTarget;
            yTarget = yTarget + (1.0/n_p);
        }
        */
        double yTarget = 0.0;
        for (int i = 0; i < n_p; i++) {
            pointsToMonitor[i][0] = 4 * Math.pow(yTarget, 4) - 12 * Math.pow(yTarget, 3)
                    + 10 * Math.pow(yTarget, 2) - 2 * yTarget + 0.55;
            pointsToMonitor[i][1] = yTarget;
            yTarget = yTarget + (1.0 / n_p);
        }
    }

    public boolean isThisAValidDecisionCommand(int r_i, double[] decisionVariables) {
        for (int i = 0; i < D; i++) {
            if (decisionVariables[i] > maxD || decisionVariables[i] < minD)
                return false;
        }
        for (int r = 0; r < nr; r++) {
            if (r != r_i)
                if (Framework.EuclideanDist2D(getLatestDecisionVariables()[r],
                        decisionVariables) < thres) {
                    return false;
                }
        }
        return true;
    }

    public double CalculateCF(double[][] loc) {
        return coreCalculation(loc);
        //return CalculateDistanceLatLon(goalPoints[0][0],loc[0][0],goalPoints[0][1],loc[0][1],0.0,0.0)+
        //        CalculateDistanceLatLon(goalPoints[1][0],loc[1][0],goalPoints[1][1],loc[1][1],0.0,0.0);
    }

    public double EvaluateCF(double[][] loc, int r_i) {
        return coreCalculation(loc);
    }


    public void fetchDecisionVector() {
        //Set the robots initial positions manually
        // randomID field in parameters file should be false
        setInitialDecisionVector(new double[][]{{0.1, 0.1}, {0.15, 0.3}, {0.7, 0.01}, {0.2, 0.18}});
    }

    public void setWriter(WriteLogFiles W) {
        this.logHelper = W;
        logHelper.WriteToFile(pointsToMonitor, "/pointsToMonitor.txt");
    }


    private double coreCalculation(double[][] loc) {
        double J = 0.0;

        int n_r = loc.length;
        double minDis, dist;
        double[] PerRobotMinDis = new double[n_r];
        for (int r = 0; r < n_r; r++) {
            PerRobotMinDis[r] = Integer.MAX_VALUE;
        }

        int[] PointAssignedToRobot = new int[n_r];
        int imin;
        for (int i = 0; i < n_p; i++) {
            minDis = EuclideanDist2D(pointsToMonitor[i], loc[0]);
            imin = 0;
            if (PerRobotMinDis[imin] > minDis) {
                PerRobotMinDis[imin] = minDis;
            }
            for (int r = 1; r < n_r; r++) {
                dist = EuclideanDist2D(pointsToMonitor[i], loc[r]);

                if (PerRobotMinDis[r] > dist) {
                    PerRobotMinDis[r] = dist;
                }

                if (dist < minDis) {
                    minDis = dist;
                    imin = r;
                }
            }
            PointAssignedToRobot[imin]++;


            J = J + minDis;
            //System.out.printf("Point:%d, minDis:%.3f, CF:%.3f\n",i,minDis,J);
        }
        for (int r = 0; r < n_r; r++) {
            if (PointAssignedToRobot[r] == 0) {
                J = J + kw * PerRobotMinDis[r];
            }
            //J = J + kw*Math.abs(loc[r][0] - xTarget);
        }
        return J;
    }


    public static double EuclideanDist2D(double[] x, double y[]) {
        return Math.sqrt(Math.pow(x[0] - y[0], 2) + Math.pow(x[1] - y[1], 2));
    }

    private double CalculateDistanceLatLon(double lat1, double lat2, double lon1,
                                           double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        Double latDistance = Math.toRadians(lat2 - lat1);
        Double lonDistance = Math.toRadians(lon2 - lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }

}

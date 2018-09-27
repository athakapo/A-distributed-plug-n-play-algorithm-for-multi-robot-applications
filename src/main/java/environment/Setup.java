package environment;

import matlabFilesBuilder.WriteLogFiles;
import org.apache.log4j.Logger;

import java.util.Properties;
import java.util.Random;

abstract public class Setup {

    private static final Logger LOG = Logger.getLogger(Setup.class);
    private WriteLogFiles logHelper;
    public int D, nr;
    public double dt;
    private double[][] lastKnownDecisions, initialDecisions;
    private Random r = new Random();


    abstract public double CalculateCF(double[][] decisionVariables);
    abstract public double EvaluateCF(double[][] decisionVariables, int r);
    abstract public boolean isThisAValidDecisionCommand(int r, double[] decisionVariables);
    abstract public void fetchDecisionVector();
    abstract public void worldConstructor();

    public void setWriter(WriteLogFiles W) {
        this.logHelper = W;
    }

    public double[][] getInitialDecisionVector(){
        return initialDecisions;
    }

    public void setInitialDecisionVector(double[][] initialDecisions){
        if (initialDecisions.length != nr){
            LOG.error("There is a mismatch between number of robots as declared in \n - the properties file " +
                    "(resources/testbeds/TESTBED_NAME/parameters.properties, field: noRobots), and \n - the manually " +
                    "passed values from the setInitialDecisionVector() function");
            LOG.error("Please correct this discrepancy and run again");
            System.exit(1);
        }else{
            if (initialDecisions[0].length != D){
                LOG.error("There is a mismatch between number of controllable variables per robot as declared in " +
                        " \n - the .properties file (resources/testbeds/TESTBED_NAME/parameters.properties, field: d), " +
                        "and \n - the manually passed values from the setInitialDecisionVector() function");
                LOG.error("Please correct this discrepancy and run again");
                System.exit(1);
            }else{
                this.initialDecisions = initialDecisions;
            }
        }
    }

    public double[][] getLatestDecisionVariables(){
        return lastKnownDecisions;
    }

    public void PassProperties(Properties props) {
        /** Passed properties:
         * 1) d: Dimension of each agent's decision vector
         * 2) nr: number of robots (agents)
         * 3) randomID: (boolean)random initial decisions, (if not) take random values from
         * 4) [minDimen, maxDimen]: boundaries of each decision variable
         */
        this.dt = Double.parseDouble(props.getProperty("dt"));
        this.D = Integer.parseInt(props.getProperty("d"));
        this.nr = Integer.parseInt(props.getProperty("noRobots"));
        if (Boolean.parseBoolean(props.getProperty("randomID"))) {
            constructRandomInitialDecisions(Double.parseDouble(props.getProperty("minDimen")),
                    Double.parseDouble(props.getProperty("maxDimen")));
        } else {
            fetchDecisionVector();
        }
    }

    private void constructRandomInitialDecisions(double minD, double maxD) {
        /** Construct random initial decisions so as:
         * 1) take values from a user-defined set
         * 2) make sure that the operational constraints (function: isThisAValidDecisionCommand) are satisfied
         */
        boolean NotValid = true;
        while(NotValid) {
            initialDecisions = new double[nr][D];
            for (int i = 0; i < nr; i++) {
                for (int j = 0; j < D; j++) {
                    initialDecisions[i][j] = minD + (maxD - minD) * r.nextDouble();
                }
            }
            updateAugmentedDecisionVector(initialDecisions);

            NotValid = false;
            for (int i = 0; i < nr; i++) {
                if (!isThisAValidDecisionCommand(i, initialDecisions[i])){
                    NotValid = true;
                    break;
                }
            }
        }

    }

    public void updateAugmentedDecisionVector(double A[][]) {
        /** Deep copy new decisions to "lastKnownDecisions" variable
         */
        int rows = A.length;
        int cols = A[0].length;
        this.lastKnownDecisions = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                lastKnownDecisions[i][j] = A[i][j];
            }
        }
    }

}

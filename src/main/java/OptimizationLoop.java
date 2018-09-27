import environment.Setup;
import loaders.ResourceLoader;
import matlabFilesBuilder.WriteLogFiles;
import optimizer.CentralizedDecisionMaking;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by atkap on 1/12/2017.
 */
public class OptimizationLoop {

    private static final Logger LOG = Logger.getLogger(OptimizationLoop.class);
    private double[] JJ;
    private String testbedName;
    private Properties propertiesFILE;
    private boolean IDecisionsPass = false;
    private double[][] initialDecisions;

    private void CoreCalculation() throws IllegalAccessException, ClassNotFoundException,
            InstantiationException{
        int Tmax = Integer.parseInt(propertiesFILE.getProperty("noIter"));
        JJ = new double[Tmax];

        boolean saveData = Boolean.parseBoolean(propertiesFILE.getProperty("saveData"));

        WriteLogFiles logHelper = new WriteLogFiles();
        if (saveData) {
            logHelper.CreateInitializeDirectory(testbedName);
        }

        // Make a new object of the selected testbed (implements environment.Setup)
        Setup testbed = (Setup) Class.forName("testbeds." + testbedName + ".Framework").newInstance();

        // Register properties from the corresponding .properties file
        testbed.PassProperties(propertiesFILE);

        // Enable Writer for 1) Cost function value and 2) decisions vector PER timestamp
        if (saveData) {
            testbed.setWriter(logHelper);
        }

        // Initialize all the needed arrays and structures for the selected testbed
        testbed.worldConstructor();

        // Pass the robots initial decisions (if any)
        if (IDecisionsPass){
            testbed.setInitialDecisionVector(initialDecisions);
        }

        //Initialize the Decision Making scheme with all the retrieved information
        CentralizedDecisionMaking planner = new CentralizedDecisionMaking(Integer.parseInt(propertiesFILE.getProperty("noRobots")),
                Integer.parseInt(propertiesFILE.getProperty("d")), Double.parseDouble(propertiesFILE.getProperty("dt")), testbed.getInitialDecisionVector(),
                Integer.parseInt(propertiesFILE.getProperty("tw")), Tmax, Integer.parseInt(propertiesFILE.getProperty("noPerturbations")),
                Integer.parseInt(propertiesFILE.getProperty("noMonomials")), Integer.parseInt(propertiesFILE.getProperty("monoMaxOrder")),
                Double.parseDouble(propertiesFILE.getProperty("pertrubConstMax")), Double.parseDouble(propertiesFILE.getProperty("pertrubConstMin")),
                Double.parseDouble(propertiesFILE.getProperty("perMono")), testbed);

        double J;
        double[][] decisionVariable = testbed.getInitialDecisionVector();
        boolean disp = Boolean.parseBoolean(propertiesFILE.getProperty("displayTime&CF"));

        //Main operation Loop
        for (int iter = 0; iter < Tmax; iter++) {
            //Calculate cost function
            testbed.updateAugmentedDecisionVector(decisionVariable);
            J = testbed.CalculateCF(decisionVariable);
            JJ[iter] = J;

            if (disp) {
                LOG.info("Testbed: " + testbedName + " | Timstamp: " + iter + " , CF: " + J);
            }

            //Keep log files
            if (saveData) {
                logHelper.WriteToFile(J, "/J.txt");
                logHelper.WriteToFile(decisionVariable, "/RobotsDecisions.txt");
            }

            //Update decision Variables
            decisionVariable = planner.produceDecisionVariables(iter, decisionVariable, J);
        }
    }

    public OptimizationLoop(String testbedName) throws IOException, IllegalAccessException, ClassNotFoundException,
            InstantiationException {

        //Retrieve experiment data from resources/testbeds/$testbedName/Parameters.properties file
        String TESTBED_PROPERTIES = "testbeds/" + testbedName + "/Parameters.properties";
        ResourceLoader resourceLoader = new ResourceLoader();
        this.propertiesFILE = resourceLoader.getPropertiesAP(TESTBED_PROPERTIES);
        this.testbedName = testbedName;
        CoreCalculation();
    }

    public OptimizationLoop(String testbedName, Properties propertiesFILE) throws IllegalAccessException, ClassNotFoundException,
            InstantiationException {

        this.testbedName = testbedName;
        this.propertiesFILE = propertiesFILE;
        CoreCalculation();
    }

    public OptimizationLoop(String testbedName, Properties propertiesFILE, double[][] initialDecisions) throws IllegalAccessException, ClassNotFoundException,
            InstantiationException {

        this.testbedName = testbedName;
        this.propertiesFILE = propertiesFILE;
        propertiesFILE.setProperty("randomID", "true");
        this.IDecisionsPass = true;
        this.initialDecisions = initialDecisions;
        CoreCalculation();
    }



    public double[] getJJ() {
        return JJ;
    }

}


import loaders.ResourceLoader;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import static junit.framework.TestCase.assertTrue;

public class Validator {

    private static final Logger LOG = LogManager.getLogger(Main.class);
    private static ResourceLoader resourceLoader;

    private static final int timeout_HOLDTHELINE = 25000; //25 sec
    private static final int timeout_ADAPTIVECOVERAGE2D = 160000; //160 sec

    @BeforeClass
    public static void Initialiazer() throws IOException {
        resourceLoader = new ResourceLoader();
        LOG.info("Entry point");
    }


    @Test(timeout = timeout_HOLDTHELINE)
    public void HoldTheLine() throws IOException, IllegalAccessException,
            ClassNotFoundException, InstantiationException {

        int[] NoRobots = new int[]{2, 4, 6, 8, 10, 12, 14, 16, 18, 20};
        double[] JCorrent = new double[]{303.8495134310999, 164.78849057293556, 113.97289336455995, 86.78895269391742,
                69.57203022873995, 58.08742655812385, 50.025536633363316, 43.901152937650316, 39.19539902982932,
                35.58899791864856};
        final double deviation = 0.10; // % around the average recorded value
        String testbedName = "HoldTheLine";

        //Retrieve experiment data from resources/testbeds/$testbedName/Parameters.properties file
        String PROBLEM_PROPERTIES = "testbeds/" + testbedName + "/Parameters.properties";
        Properties nv = resourceLoader.getPropertiesAP(PROBLEM_PROPERTIES);
        nv.setProperty("randomID", "true");
        nv.setProperty("displayTime&CF", "false");
        int Tmax = Integer.parseInt(nv.getProperty("noIter"));

        for (int i = 0; i < NoRobots.length; i++) {
            nv.setProperty("noRobots", Integer.toString(NoRobots[i]));
            OptimizationLoop test = new OptimizationLoop(testbedName, nv);
            double curCF = test.getJJ()[Tmax - 1];
            LOG.info("[Number of robots: " + NoRobots[i] + "] Average CF value: " + JCorrent[i] +
                    " | Recorded CF value: " + curCF);
            assertTrue(curCF >= JCorrent[i] * (1 - deviation) && curCF <= JCorrent[i] * (1 + deviation));
        }

    }


    @Test(timeout = timeout_ADAPTIVECOVERAGE2D)
    public void AdaptiveCoverage2D() throws IOException, IllegalAccessException,
            ClassNotFoundException, InstantiationException {
        final String testbedName = "AdaptiveCoverage2D";
        int[] NoRobots = new int[]{5, 10, 15, 20};
        double[] DesiredCF = new double[]{1964.9232312423503, 1436.8281919714111, 1174.6110007708967, 1024.7571168934032};
        final double deviation = 0.15; // % around the average recorded value

        ArrayList<double[][]> InitialRobotPos = new ArrayList<>();
        InitialRobotPos.add(new double[][]{
                {0.614911829488112, 0.8133550245441475},
                {0.005173486721660847, 0.8708144782022111},
                {0.7813029165430574, 0.7235740364415637},
                {0.6102258569310325, 0.6831150957320858},
                {0.307518832948647, 0.700133987499451}
        });  //5 robots

        InitialRobotPos.add(new double[][]{
                {0.748782920935759, 0.523484207016743},
                {0.521725763522798, 0.662285767439358},
                {0.618314982927528, 0.850496608951951},
                {0.987035204212623, 0.683118335623724},
                {0.560976459754866, 0.480022865952500},
                {0.796815587856705, 0.712148754079348},
                {0.904113237958921, 0.006839213657844},
                {0.687208306090933, 0.641243548188644},
                {0.822574509070901, 0.141788922472766},
                {0.863995313984828, 0.247451873545336}
        });  //10 robots

        InitialRobotPos.add(new double[][]{
                {0.14577420526318996, 0.576278714243178},
                {0.2287192796228119, 0.06335597195780196},
                {0.29834185863262586, 0.9628540563669717},
                {0.9931435119469243, 0.5494579142029646},
                {0.5462225494429768, 0.9426836186567821},
                {0.27633928302537847, 0.11416809281198337},
                {0.442823130636833, 0.12707121966850377},
                {0.023518096367830754, 0.3668238377023627},
                {0.2736123374545356, 0.42159878036987675},
                {0.13987608359458115, 0.7194024473396126},
                {0.9123916888742124, 0.6759966235761181},
                {0.5827780579029868, 0.7440031931387296},
                {0.6982890039215549, 0.5591813033016326},
                {0.5247276767048001, 0.17628825739163978},
                {0.7932215497908259, 0.8860535121525006}
        });  //15 robots

        InitialRobotPos.add(new double[][]{
                {0.4302940553071771, 0.9336551468434044},
                {0.27904083669451984, 0.14355607681714966},
                {0.033174826178091976, 0.052435675144100524},
                {0.689857943305758, 0.9094755524109361},
                {0.016987800455446123, 0.502147576093662},
                {0.6729042340902326, 0.523797930027083},
                {0.5270870474503296, 0.6764456476961492},
                {0.1708931275935235, 0.8223618892029051},
                {0.7729124297670855, 0.6385338387254741},
                {0.9653252376509153, 0.4834610954269666},
                {0.9404450108644948, 0.5021734528331218},
                {0.25078272365907783, 0.2098095332401122},
                {0.31744707644427084, 0.8840957922768341},
                {0.5021017372382002, 0.4307793042384067},
                {0.9442617745352636, 0.9722833733626007},
                {0.6312799166890012, 0.14798769712391624},
                {0.2914232568431935, 0.044622775784423196},
                {0.3850766905967481, 0.22797575087108346},
                {0.43505529095951967, 0.593721720238489},
                {0.16961670019569774, 0.657633143182973},
        });  //20 robots

        //Retrieve experiment data from resources/testbeds/$testbedName/Parameters.properties file
        String PROBLEM_PROPERTIES = "testbeds/" + testbedName + "/Parameters.properties";
        Properties nv = resourceLoader.getPropertiesAP(PROBLEM_PROPERTIES);
        nv.setProperty("randomID", "true");
        nv.setProperty("displayTime&CF", "false");
        int Tmax = Integer.parseInt(nv.getProperty("noIter"));

        for (int i = 0; i < NoRobots.length; i++) {
            nv.setProperty("noRobots", Integer.toString(NoRobots[i]));
            OptimizationLoop test = new OptimizationLoop(testbedName, nv, InitialRobotPos.get(i));
            double curCF = test.getJJ()[Tmax - 1];
            LOG.info("[Number of robots: " + NoRobots[i] + "] Average CF value: " + DesiredCF[i] +
                    " | Recorded CF value: " + curCF);
            assertTrue(curCF >= DesiredCF[i] * (1 - deviation) && curCF <= DesiredCF[i] * (1 + deviation));
        }
    }

}

import loaders.ResourceLoader;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;
import java.util.Properties;

public class Main {
    private static final Logger LOG = Logger.getLogger(Main.class);
    private static final String PROPERTIES_LOG = "logging.properties";

    public static void main(String args[]) throws IOException, IllegalAccessException,
            ClassNotFoundException, InstantiationException {

        ResourceLoader resourceLoader = new ResourceLoader();
        PropertyConfigurator.configure(resourceLoader.getProperties(PROPERTIES_LOG));
        LOG.info("Entry point");

        String testbed;
        if (args.length == 1) {
            testbed = args[0];
        } else {
            LOG.error("Re-run the application having as argument ONLY the name of the the testbed (e.g. \"HoldTheLine\")\n");
            return;
        }
        new OptimizationLoop(testbed);

    }

}

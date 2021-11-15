import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;

public class Main {
    private static final Logger LOG = LogManager.getLogger(Main.class);

    public static void main(String args[]) throws IOException, IllegalAccessException,
            ClassNotFoundException, InstantiationException {

        LOG.trace("Entry point");
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

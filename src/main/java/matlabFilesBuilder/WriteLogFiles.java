package matlabFilesBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Created by atkap on 6/22/2017.
 */
public class WriteLogFiles {

    private String logPath;

    public WriteLogFiles() {
    }


    public void CreateInitializeDirectory(String testbedName) {
        DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
        logPath = "../LogsDistributedInformedCAO/" + testbedName + "/" + System.getProperty("user.name") + "@" +
                dateFormat.format(new Date());
        boolean success = (new File(logPath)).mkdirs();
        if (!success) {
            System.err.format("The log folder cannot be created\n");
            return;
        }
        try {
            Files.copy((new File("src/main/resources/testbeds/" + testbedName + "/Parameters.properties")).toPath(),
                    (new File(logPath + "/Parameters.properties")).toPath(), REPLACE_EXISTING);

            //Files.copy((new File("src/main/resources/matlabHelpers/plotDataMatlab.m")).toPath(),
            //        (new File(logPath+"/plotDataMatlab.m")).toPath(), REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void WriteToFile(double[][] x, String FILENAME) {

        FILENAME = logPath + FILENAME;

        BufferedWriter bw = null;
        FileWriter fw = null;

        try {
            File file = new File(FILENAME);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            // true = append file
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);

            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < x.length; i++) {
                for (int j = 0; j < x[0].length; j++) {
                    builder.append(x[i][j] + " ");
                }
            }
            bw.write(builder.toString());

            bw.newLine();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }


    public void WriteToFile(double x, String FILENAME) {

        FILENAME = logPath + FILENAME;

        BufferedWriter bw = null;
        FileWriter fw = null;

        try {
            File file = new File(FILENAME);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            // true = append file
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);

            bw.write(String.valueOf(x));
            bw.newLine();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }


}

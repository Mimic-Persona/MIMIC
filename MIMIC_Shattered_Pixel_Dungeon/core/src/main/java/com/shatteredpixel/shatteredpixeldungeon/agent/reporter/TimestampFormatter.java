package com.shatteredpixel.shatteredpixeldungeon.agent.reporter;

import com.shatteredpixel.shatteredpixeldungeon.agent.utils.directorHandler;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.poi.util.IOUtils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.util.List;

public class TimestampFormatter {

    static String META_ROOT_PATH = "./jacoco-metaData/";

    static String EXEC_ROOT_PATH = "./jacoco-reports/";

    static List<String> metaDataNames = directorHandler.fetchFileNames(META_ROOT_PATH);

    /**
     * Updates the nanoseconds if they are negative in the metadata file by adding the previous timestamp to the current timestamp.
     * @param metaDataPath the path to the metadata file
     */
    public static void updateNanosecondsInMetaData(String metaDataPath) {

        // Read the metadata file
        File metaDataFile = new File(metaDataPath);

        // Check if the metadata file exists
        if (!metaDataFile.exists()) {
            System.err.println("Metadata file does not exist: " + metaDataPath);
            return;
        }

        System.out.println("Updating nanoseconds in metadata file: " + metaDataPath);

        // Change the metadata file line by line
        try {
            // Read the metadata file line by line
            String[] metaDataLines = FileUtils.readFileToString(metaDataFile, StandardCharsets.UTF_8).split("\n");
            long prevTimestamp = -1L;
            long firstTimestamp = (new JSONObject(metaDataLines[0])).getLong("timestamp");

            for (int i = 0; i < metaDataLines.length; i++) {
                String metaDataLine = metaDataLines[i];

                if (metaDataLine.isEmpty()) {
                    continue;
                }

                // Parse the metadata as a JSON object
                JSONObject metaDataJson = new JSONObject(metaDataLine);

                // Get the timestamp from the metadata
                long timestamp = metaDataJson.getLong("timestamp");

                // Get the nanoseconds from the metadata
                long nanoseconds = timestamp - firstTimestamp;

                if (metaDataJson.getLong("nanoseconds") != nanoseconds) {
                    System.out.println("Updated nanoseconds at timestamp: " + timestamp + " in " + metaDataPath + " from " + metaDataJson.getLong("nanoseconds") + " to " + (nanoseconds));
                    metaDataJson.put("nanoseconds", String.valueOf(nanoseconds));
                }


                if (nanoseconds < 0) {
                    // Update the nanoseconds in the metadata
                    metaDataJson.put("nanoseconds", String.valueOf(prevTimestamp + timestamp));

                    System.out.println("Updated nanoseconds at timestamp: " + timestamp + " in " + metaDataPath + " from " + nanoseconds + " to " + (prevTimestamp + timestamp));

                } else {
                    // Get the previous timestamp if the nanoseconds are not negative
                    prevTimestamp = nanoseconds;
                }

                metaDataLines[i] = metaDataJson.toString();
            }

            // Write the updated metadata to the file with each element in the array separated by a newline
            String result = String.join("\n", metaDataLines);
            FileUtils.write(metaDataFile, result, StandardCharsets.UTF_8);

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the nanoseconds if they have intervals larger than 1 minute in the metadata file.
     * @param metaDataPath the path to the metadata file
     */
    public static void updateNanosecondsPauseInMetaData(String metaDataPath, long time) {

        // Read the metadata file
        File metaDataFile = new File(metaDataPath);

        // Check if the metadata file exists
        if (!metaDataFile.exists()) {
            System.err.println("Metadata file does not exist: " + metaDataPath);
            return;
        }

        System.out.println("Updating pause in metadata file: " + metaDataPath);

        // Change the metadata file line by line
        try {
            // Read the metadata file line by line
            String[] metaDataLines = FileUtils.readFileToString(metaDataFile, StandardCharsets.UTF_8).split("\n");
            long prevNano = 0;
            long pauseNano = 0;

            for (int i = 0; i < metaDataLines.length; i++) {
                String metaDataLine = metaDataLines[i];

                if (metaDataLine.isEmpty()) {
                    continue;
                }

                // Parse the metadata as a JSON object
                JSONObject metaDataJson = new JSONObject(metaDataLine);

                // Get the timestamp from the metadata
                long timestamp = metaDataJson.getLong("timestamp");

                // Get the nanoseconds from the metadata
                long nanoseconds = metaDataJson.getLong("nanoseconds");

//                if (timestamp == 75372963721600L || timestamp == 78626093766000L || timestamp == 74452831743900L) {
//                    System.out.println("nanoseconds: " + nanoseconds);
//                }

                // If prevNano to nanoseconds is greater than 1 minute
                if (nanoseconds - pauseNano - prevNano > time) {

                    long newNano = prevNano + 15_000_000_000L;

                    metaDataJson.put("nanoseconds", String.valueOf(newNano));

                    // Record the total paused time applied to the nanoseconds
                    pauseNano = nanoseconds - newNano;

                    System.out.println("Updated nanoseconds at timestamp: " + timestamp + " in " + metaDataPath + " from " + nanoseconds + " to " + (newNano) + " with pause: " + pauseNano);

                } else {
                    // Update the nanoseconds in the metadata to the original nanoseconds - the total paused time
                    long newNano = nanoseconds - pauseNano;

                    // If changed
                    if (metaDataJson.getLong("nanoseconds") != newNano) {
                        metaDataJson.put("nanoseconds", String.valueOf(newNano));
                        System.out.println("Updated nanoseconds at timestamp: " + timestamp + " in " + metaDataPath + " from " + nanoseconds + " to " + (newNano));
                    } else {
                        System.out.println("No change in nanoseconds at timestamp: " + timestamp + " in " + metaDataPath + " with " + nanoseconds);
                    }


                }

                prevNano = metaDataJson.getLong("nanoseconds");

                metaDataLines[i] = metaDataJson.toString();
            }

            // Write the updated metadata to the file with each element in the array separated by a newline
            String result = String.join("\n", metaDataLines);
            FileUtils.write(metaDataFile, result, StandardCharsets.UTF_8);

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public static void updateTimestampInExecFileName(String subject, String execFilePath, String metaDataPath, String xlsxPath) {

        String[] execFileNames = directorHandler.fetchFileNames(execFilePath + subject + "/").toArray(new String[0]);


        // If the metadata path is null, it means the exec file names should be edited according to the xlsx file
        if (metaDataPath == null) {
            IOUtils.setByteArrayMaxOverride(200_000_000);

            try (
                    FileInputStream fis = new FileInputStream(xlsxPath);
                    Workbook workbook = new XSSFWorkbook(fis)
            ) {

                Sheet sheet = workbook.getSheet(subject);

                // Iterate through each row in the sheet
                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                    Row row = sheet.getRow(i);

                    Cell nanosecondsCell = row.getCell(0);
                    Cell timestampCell = row.getCell(1);

                    if (timestampCell != null && nanosecondsCell != null) {
                        // Get the cell values
                        long nanoseconds = (long) nanosecondsCell.getNumericCellValue();
                        long timestamp = (long) timestampCell.getNumericCellValue();

                        for (String execFileName : execFileNames) {
                            // Get the timestamp from the exec file name
                            long execTimestamp = Long.parseLong(execFileName.split("_")[1].replace(".exec", ""));

                            if (timestamp == execTimestamp) {
                                // Update the timestamp in the file name
                                File execFile = new File(execFilePath + subject + "/" + execFileName);
                                String newExecFileName = execFileName.replace("_" + execTimestamp, "_" + nanoseconds);
                                execFile.renameTo(new File(execFilePath + subject + "/" + newExecFileName));

                                System.out.println("Updated timestamp in exec file name at timestamp: " + timestamp + " in " + subject + " to " + nanoseconds);

                                break;
                            }
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }


        } else {
            // Get the metadata file
            File metaDataFile = new File(metaDataPath + subject + ".txt");

            // Check if the metadata file exists
            if (!metaDataFile.exists()) {
                System.err.println("Metadata file does not exist: " + metaDataPath);
                return;
            }

            // Read the metadata file line by line
            try {

                // Read the metadata file line by line
                String[] metaDataLines = FileUtils.readFileToString(metaDataFile, StandardCharsets.UTF_8).split("\n");

                for (String metaDataLine : metaDataLines) {
                    if (metaDataLine.isEmpty()) {
                        continue;
                    }

                    // Parse the metadata as a JSON object
                    JSONObject metaDataJson = new JSONObject(metaDataLine);

                    // Get the timestamp from the metadata
                    long timestamp = metaDataJson.getLong("timestamp");

                    // Get the nanoseconds from the metadata
                    long nanoseconds = metaDataJson.getLong("nanoseconds");

                    for (String execFileName : execFileNames) {
                        // Get the timestamp from the exec file name
                        long execTimestamp = Long.parseLong(execFileName.split("_")[1].replace(".exec", ""));

                        if (timestamp == execTimestamp) {
                            // Update the timestamp in the file name
                            File execFile = new File(execFilePath + subject + "/" + execFileName);
                            String newExecFileName = execFileName.replace("_" + execTimestamp, "_" + nanoseconds);
//                            newExecFileName = newExecFileName.replace("Monkey", "MonkeyP");
//                            newExecFileName = newExecFileName.replace("PP", "P");
                            execFile.renameTo(new File(execFilePath + subject + "/" + newExecFileName));

                            System.out.println("Updated timestamp in exec file name at timestamp: " + timestamp + " in " + subject + " to " + nanoseconds);

                            break;
                        }
                    }
                }

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }


        }

    }

    public static void main(String[] args) throws IOException {
        for (String metaDataName : metaDataNames){

            String metaDataPath = META_ROOT_PATH + metaDataName;

            if (!metaDataName.contains("Human")) {
                continue;
            }

            if (metaDataName.contains("Hybrid")) {
                updateNanosecondsInMetaData(metaDataPath);
                updateNanosecondsPauseInMetaData(metaDataPath, 60_000_000_000L);

            } else if (metaDataName.contains("Human")) {
                updateNanosecondsInMetaData(metaDataPath);
                updateNanosecondsPauseInMetaData(metaDataPath, 180_000_000_000L);

            } else {
                updateNanosecondsInMetaData(metaDataPath);
            }
        }

        // Get the subject names
        List<String> subjectNames = directorHandler.fetchDirectoryNames(EXEC_ROOT_PATH);
        for (String subject : subjectNames) {
            System.out.println("Updating timestamps in exec file names for: " + subject);

            if (subject.contains("Human")) {
                updateTimestampInExecFileName(subject, EXEC_ROOT_PATH, META_ROOT_PATH, null);
            }

        }

//        updateTimestampInExecFileName("MonkeyP1a", "F:\\Generative Agents\\SPD DATA\\jacoco-reports/", "jacoco-metaData/", null);

//        updateTimestampInExecFileName("AggressionHybrid1", "jacoco-reports/", null, "jacoco-reports/modified_coverage.xlsx");
//        updateTimestampInExecFileName("CautionHybrid1", "jacoco-reports/", null, "jacoco-reports/modified_coverage.xlsx");

    }
}

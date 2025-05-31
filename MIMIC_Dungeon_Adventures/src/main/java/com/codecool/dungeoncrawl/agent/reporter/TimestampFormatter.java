package com.codecool.dungeoncrawl.agent.reporter;

import com.codecool.dungeoncrawl.agent.utils.directorHandler;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TimestampFormatter {

    static List<String> metaDataNames = directorHandler.fetchFileNames("jacoco-metaData/");

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
//                            newExecFileName = newExecFileName.replace("AggressionHybrid2", "AggressionHybrid1");
//                            newExecFileName = newExecFileName.replace("CautionHybrid2", "CautionHybrid1");
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
        // Update the nanoseconds with negative values in the metadata files
        for (String metaDataName : metaDataNames){
            String metaDataPath = "jacoco-metaData/" + metaDataName;
            updateNanosecondsInMetaData(metaDataPath);
        }

        // Get the subject names
        List<String> subjectNames = directorHandler.fetchDirectoryNames("G:/DA_DATA/jacoco-reports/");

        // Update the timestamps in the exec file names
        for (String subject : subjectNames) {
            System.out.println("Updating timestamps in exec file names for: " + subject);

            if (subject.contains("Monkey") && !subject.contains("MonkeyP")) {
                updateTimestampInExecFileName(subject, "G:/DA_DATA/jacoco-reports/", "jacoco-metaData/", null);
            }
        }

//        updateTimestampInExecFileName("MonkeyP1a", "F:\\Generative Agents\\SPD DATA\\jacoco-reports/", "jacoco-metaData/", null);

//        updateTimestampInExecFileName("AggressionHybrid1", "jacoco-reports/", null, "jacoco-reports/modified_coverage.xlsx");
//        updateTimestampInExecFileName("CautionHybrid1", "jacoco-reports/", null, "jacoco-reports/modified_coverage.xlsx");

    }
}

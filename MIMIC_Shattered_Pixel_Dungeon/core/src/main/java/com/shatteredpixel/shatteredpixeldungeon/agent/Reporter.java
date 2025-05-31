package com.shatteredpixel.shatteredpixeldungeon.agent;

import com.shatteredpixel.shatteredpixeldungeon.agent.reporter.JacocoCoverageFetcher;
import com.shatteredpixel.shatteredpixeldungeon.agent.reporter.JacocoReportGenerator;
import com.shatteredpixel.shatteredpixeldungeon.agent.reporter.JacocoReporter;
import com.shatteredpixel.shatteredpixeldungeon.agent.utils.directorHandler;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.shatteredpixel.shatteredpixeldungeon.agent.utils.directorHandler.deleteDirectoryRecursively;

public class Reporter {

    // "AggressionHybrid1", "CautionHybrid1"
    public static final String[] SUBJECTS = {
//            "MonkeyP1a",
//            "MonkeyP1b",
//            "MonkeyP1c",
//            "MonkeyP2a",
//            "MonkeyP2b",
//            "MonkeyP2c",
//            "MonkeyP3a",
//            "MonkeyP3b",
//            "MonkeyP3c",
//            "MonkeyP4a",
//            "MonkeyP4b",
//            "MonkeyP4c",
//            "MonkeyP5a",
//            "MonkeyP5b",
//            "MonkeyP5c",
//            "MonkeyP6a",
//            "MonkeyP6b",
//            "MonkeyP6c",
            "MonkeyP7a",
//            "MonkeyP7b",
//            "MonkeyP7c"
    };

    private static final String META_DATA_DIR = "jacoco-metaData/";
    private static final String REPORT_DIR = "jacoco-reports/";
    private static final String TEMP_REPORT_DIR = "G:\\SPD_DATA\\jacoco-reports-merged/";
    private static final String SOURCE_FILES_DIR = "core/src/main/java/com/shatteredpixel/shatteredpixeldungeon";
    private static final String CLASS_FILES_DIR = "core/build/classes/java/main/com/shatteredpixel/shatteredpixeldungeon";
    private static final String HTML_REPORT_DIR = "G:\\SPD_DATA/jacoco-reports-html/";
    private static final String XML_REPORT_DIR = "G:\\SPD_DATA/jacoco-reports-xml/";
    private static final String EXCEL_FILE_PATH = "jacoco-reports/modified_coverage.xlsx";
    public static boolean IS_OLD = false;

    private static boolean isFirstRow = true;
    private static int rowNum = 1;

    private static void writeExcelFromHTML(String subject) throws IOException, JSONException {
        FileInputStream inputStream;
        Workbook workbook;
        List<String> folderNames;
        long firstTimeStamp = -1;

        inputStream = new FileInputStream(EXCEL_FILE_PATH);
        workbook = new XSSFWorkbook(inputStream);
        folderNames = directorHandler.fetchDirectoryNames(HTML_REPORT_DIR + subject + "/");

        System.out.println("Folder names: " + folderNames);

        Sheet sheet = workbook.getSheet(subject);

        // Create the sheet if it does not exist
        if (sheet == null) {
            sheet = workbook.createSheet(subject);
        }

        int cellNum = 0;

        // Clean the sheet if it is the first row;
        // Create the header row if it is the first row
        if (isFirstRow) {
            workbook.removeSheetAt(workbook.getSheetIndex(subject));
            sheet = workbook.createSheet(subject);

            Row headerRow = sheet.createRow(0);
            for (String key : JacocoReporter.keys) {
                Cell cell = headerRow.createCell(cellNum++);
                cell.setCellValue(key);
            }

            isFirstRow = false;
        }

        for (String folderName : folderNames) {

            // Fetch the coverage data and write it to the Excel file
            JSONObject covData = JacocoCoverageFetcher.fetchTotalCoverage(HTML_REPORT_DIR + subject + "/" + folderName);

            // Create the data row
            Row dataRow = sheet.createRow(rowNum++);
            cellNum = 0;

            for (String key : JacocoReporter.keys) {
                Cell cell = dataRow.createCell(cellNum++);

                if (key.equals("Action ID")) {
                    cell.setCellValue(rowNum - 1);
                    continue;
                }

                if (key.equals("Action")) {
                    cell.setCellValue("");
                    continue;
                }

                if (key.equals("Env")) {
                    cell.setCellValue("");
                    continue;
                }

                long currTimeStamp = Long.parseLong(folderName.split("_")[1]);

                if (key.equals("Timestamp")) {

                    if (firstTimeStamp == -1) {
                        firstTimeStamp = currTimeStamp;
                    }
                    cell.setCellValue(currTimeStamp);
                    continue;
                }

                if (key.equals("Nanoseconds")) {
                    cell.setCellValue(currTimeStamp - firstTimeStamp);
                    continue;
                }


                cell.setCellValue(covData.getDouble(key));
            }
        }

        // Write the workbook to the file
        try (FileOutputStream fileOut = new FileOutputStream(EXCEL_FILE_PATH)) {
            workbook.write(fileOut);
        }

        // Closing the workbook
        workbook.close();
    }

    public static void writeExcelFromXML(String subject, String excelFilePath, String xmlDirectoryPath, boolean isFirstRow, boolean hasMetaData, int rowNum) throws Exception {
        FileInputStream inputStream;
        Workbook workbook;
        List<String> fileNames;

        IOUtils.setByteArrayMaxOverride(200_000_000);
        ZipSecureFile.setMinInflateRatio(0.00001);
        inputStream = new FileInputStream(excelFilePath);
        workbook = new XSSFWorkbook(inputStream);
        fileNames = directorHandler.fetchFileNames(xmlDirectoryPath);

        System.out.println("File names: " + fileNames);

        Sheet sheet = workbook.getSheet(subject);

        // Create the sheet if it does not exist
        if (sheet == null) {
            sheet = workbook.createSheet(subject);
        }

        int cellNum = 0;

        // Clean the sheet if it is the first row;
        // Create the header row if it is the first row
        if (isFirstRow) {
            workbook.removeSheetAt(workbook.getSheetIndex(subject));
            sheet = workbook.createSheet(subject);

            Row headerRow = sheet.createRow(0);
            for (String key : JacocoReporter.keys) {
                Cell cell = headerRow.createCell(cellNum++);
                cell.setCellValue(key);
            }

            isFirstRow = false;
        }

        // TODO: TO BE REMOVED
        if (!hasMetaData) {
            long firstTimeStamp = -1;

            for (String fileName : fileNames) {
                // Fetch the coverage data and write it to the Excel file
                JSONObject covData = JacocoCoverageFetcher.fetchTotalCoverageFromXML(xmlDirectoryPath + fileName);

                // Create the data row
                Row dataRow = sheet.createRow(rowNum++);
                cellNum = 0;

                for (String key : JacocoReporter.keys) {
                    Cell cell = dataRow.createCell(cellNum++);

                    if (key.equals("Action ID")) {
                        cell.setCellValue(rowNum - 2);
                        continue;
                    }

                    if (key.equals("Action")) {
                        cell.setCellValue("");
                        continue;
                    }

                    if (key.equals("Env")) {
                        cell.setCellValue("");
                        continue;
                    }

                    long currTimeStamp = Long.parseLong(fileName.split("_")[1].replace(".xml", ""));

                    if (key.equals("Timestamp")) {

                        if (firstTimeStamp == -1) {
                            firstTimeStamp = currTimeStamp;
                        }
                        cell.setCellValue(currTimeStamp);
                        continue;
                    }

                    if (key.equals("Nanoseconds")) {
                        if (firstTimeStamp == -1) {
                            cell.setCellValue(0);
                        } else {
                            cell.setCellValue(currTimeStamp - firstTimeStamp);
                        }
                        continue;
                    }


                    cell.setCellValue(covData.getDouble(key));
                }
            }

            // Write the workbook to the file
            try (FileOutputStream fileOut = new FileOutputStream(excelFilePath)) {
                workbook.write(fileOut);
            }

            // Closing the workbook
            workbook.close();

            return;
        }

        BufferedReader reader = new BufferedReader(new FileReader(META_DATA_DIR + subject + ".txt"));

        for (String fileName : fileNames) {
            String line = reader.readLine();
            JSONObject metaData = new JSONObject(line);

            // Fetch the coverage data and write it to the Excel file
            JSONObject covData = JacocoCoverageFetcher.fetchTotalCoverageFromXML(xmlDirectoryPath + fileName);

            // Create the data row
            Row dataRow = sheet.createRow(rowNum++);
            cellNum = 0;

            for (String key : JacocoReporter.keys) {
                Cell cell = dataRow.createCell(cellNum++);

                if (key.equals("Action ID")) {
                    cell.setCellValue(Integer.parseInt(metaData.getString("actionCounter")));
                    continue;
                }

                if (key.equals("Action")) {
                    cell.setCellValue(metaData.getString("action"));
                    continue;
                }

                if (key.equals("Env")) {
                    cell.setCellValue(metaData.getString("env"));
                    continue;
                }

                if (key.equals("Timestamp")) {
                    cell.setCellValue(Long.parseLong(metaData.getString("timestamp")));
                    continue;
                }

                if (key.equals("Nanoseconds")) {
                    cell.setCellValue(Long.parseLong(metaData.getString("nanoseconds")));
                    continue;
                }


                cell.setCellValue(covData.getDouble(key));
            }
        }

        // Write the workbook to the file
        try (FileOutputStream fileOut = new FileOutputStream(EXCEL_FILE_PATH)) {
            workbook.write(fileOut);
        }

        // Closing the workbook
        workbook.close();
        reader.close();
    }

    public static void writeCsvFromXML(String subject, String csvFilePath, String xmlDirectoryPath, boolean isFirstRow, boolean hasMetaData, int rowNum) throws Exception {

        IOUtils.setByteArrayMaxOverride(200_000_000);
        ZipSecureFile.setMinInflateRatio(0.00001);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePath, !isFirstRow))) {
            List<String> fileNames = directorHandler.fetchFileNames(xmlDirectoryPath);
            System.out.println("File names: " + fileNames);

            if (isFirstRow) {
                // Write header row
                writer.write(String.join(",", JacocoReporter.keys) + "\n");
                isFirstRow = false;
            }

            BufferedReader reader = null;
            if (hasMetaData) {
                reader = new BufferedReader(new FileReader(META_DATA_DIR + subject + ".txt"));
            }

            long firstTimeStamp = -1;

            for (String fileName : fileNames) {

                JSONObject covData = JacocoCoverageFetcher.fetchTotalCoverageFromXML(xmlDirectoryPath + fileName);

                // Prepare to read metadata if necessary
                JSONObject metaData = null;
                if (hasMetaData) {
                    String line = reader.readLine();
                    metaData = new JSONObject(line);
                }

                // Prepare data for each row
                StringBuilder row = new StringBuilder();
                for (String key : JacocoReporter.keys) {
                    String cellValue = "";
                    if (key.equals("Action ID")) {
                        cellValue = hasMetaData ? metaData.getString("actionCounter") : String.valueOf(rowNum - 1);

                    } else if (key.equals("Action")) {
                        cellValue = hasMetaData ? metaData.getString("action") : "";

                    } else if (key.equals("Env")) {
                        cellValue = hasMetaData ? metaData.getString("env") : "";

                    } else if (key.equals("Timestamp")) {
                        long currTimeStamp = Long.parseLong(fileName.split("_")[1].replace(".xml", ""));
                        if (firstTimeStamp == -1) {
                            firstTimeStamp = currTimeStamp;
                        }
                        cellValue = String.valueOf(hasMetaData ? Long.parseLong(metaData.getString("timestamp")) : currTimeStamp);

                    } else if (key.equals("Nanoseconds")) {
                        if (firstTimeStamp != -1) {
                            long currTimeStamp = Long.parseLong(fileName.split("_")[1].replace(".xml", ""));
                            cellValue = String.valueOf(currTimeStamp - firstTimeStamp);

                        } else {
                            cellValue = "0";
                        }

                    } else {
                        cellValue = String.valueOf(covData.getDouble(key));
                    }

                    row.append(cellValue).append(",");
                }

                // Remove trailing comma and add new line
                row.deleteCharAt(row.length() - 1).append("\n");

                // Write row to CSV file
                writer.write(row.toString());
                rowNum++;
            }

            if (reader != null) {
                reader.close();
            }
        }
    }

    private static void report(String subject) throws Exception {
        List<String> fileNames = directorHandler.fetchFileNames(REPORT_DIR + subject + "/");
        Path temp_path = Paths.get(TEMP_REPORT_DIR + subject + "/");

        if (!Files.exists(temp_path)) {
            Files.createDirectories(temp_path);
        }

        int counter = 1;
        // Merge the exec files into a new directory and generate the Jacoco HTML report
        for (String fileName : fileNames) {

            System.out.println(GLog.GREEN + "======================================================================== "
                    + "#FILE " + counter + " / " + fileNames.size()
                    + " ========================================================================"
                    + GLog.RESET);

            JacocoReportGenerator.generateJacocoXMLReport(CLASS_FILES_DIR, REPORT_DIR + subject + "/",
                    SOURCE_FILES_DIR, XML_REPORT_DIR + subject + "/" + fileName.replace(".exec", ".xml"), fileName);

            counter++;

//            if (counter > 10) {
//                break;
//            }
        }

        writeExcelFromXML(subject, EXCEL_FILE_PATH, XML_REPORT_DIR + subject + "/", isFirstRow, !IS_OLD, rowNum);

        deleteDirectoryRecursively(temp_path);
    }

    public static void main(String[] args) throws Exception {
        for (String subject : SUBJECTS) {
            isFirstRow = true;
            rowNum = 1;

            // "Achievement", "Adrenaline", "Aggression", "Caution", "Completion", "Curiosity", "Efficiency"
            if (subject.startsWith("AggressionHybrid1") || subject.startsWith("CautionHybrid1")) {
                IS_OLD = true;
                report(subject);
            } else {
                IS_OLD = false;
                report(subject);
            }
        }
    }
}

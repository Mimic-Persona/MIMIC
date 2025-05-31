package com.shatteredpixel.shatteredpixeldungeon.agent.reporter;

import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.tools.ExecDumpClient;
import org.jacoco.core.tools.ExecFileLoader;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static com.shatteredpixel.shatteredpixeldungeon.agent.reporter.JacocoCoverageFetcher.fetchTotalCoverage;
import static com.shatteredpixel.shatteredpixeldungeon.agent.reporter.JacocoReportGenerator.generateJacocoHtmlReport;
import static com.shatteredpixel.shatteredpixeldungeon.agent.utils.FileHandler.appendToFile;


public class JacocoReporter {
    // TODO: Define the constants before running the experiment
//    private static File lastReportFile = new File("jacoco-reports/CautionHybrid1/CautionHybrid1-2024-08-27-00-15-23-780_923780035700.exec");
    private static File lastReportFile = null;
    private static final String ADDRESS = "localhost";
    private static final int PORT = 6300;
    private static final String REPORT_DIR = "../jacoco-reports/" + ReporterClient.PREFIX;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-SSS_NNNNNNNNNN");

    private static final String SOURCE_FILES_DIR = "core/src/main/java/com/shatteredpixel/shatteredpixeldungeon";
    private static final String CLASS_FILES_DIR = "core/build/classes/java/main/com/shatteredpixel/shatteredpixeldungeon";
    private static final String EXEC_FILES_DIR = "../jacoco-reports/" + ReporterClient.PREFIX;
    private static final String OUT_DIR = "F:/Generative Agents/SPD DATA/jacoco-reports-html/" + ReporterClient.PREFIX + "/" + ReporterClient.PREFIX;
    private static final String EXCEL_FILE_PATH = "../jacoco-reports/coverage.xlsx";
    private static final String META_DATA_FILE = "../jacoco-metaData/" + ReporterClient.PREFIX + ".txt";

    private static final String[] CLASS_FILES_DIRS = {
            "core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors",
            "core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/effects",
            "core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items",
            "core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels",
            "core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/mechanics",
            "core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/plants",
            "core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/scenes",
            "core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites",
            "core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/tiles",
            "core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui",
            "core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/utils",

            "core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Assets.java",
            "core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Badges.java",
            "core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Bones.java",
            "core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Challenges.java",
            "core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Chrome.java",
            "core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Dungeon.java",
            "core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/GamesInProgress.java",
            "core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/QuickSlot.java",
            "core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Rankings.java",
            "core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ShatteredPixelDungeon.java",
            "core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Statistics.java"
    };


    private static final String[] SOURCE_FILES_DIRS = {
            "core/build/classes/java/main/com/shatteredpixel/shatteredpixeldungeon/actors",
            "core/build/classes/java/main/com/shatteredpixel/shatteredpixeldungeon/effects",
            "core/build/classes/java/main/com/shatteredpixel/shatteredpixeldungeon/items",
            "core/build/classes/java/main/com/shatteredpixel/shatteredpixeldungeon/levels",
            "core/build/classes/java/main/com/shatteredpixel/shatteredpixeldungeon/mechanics",
            "core/build/classes/java/main/com/shatteredpixel/shatteredpixeldungeon/plants",
            "core/build/classes/java/main/com/shatteredpixel/shatteredpixeldungeon/scenes",
            "core/build/classes/java/main/com/shatteredpixel/shatteredpixeldungeon/sprites",
            "core/build/classes/java/main/com/shatteredpixel/shatteredpixeldungeon/tiles",
            "core/build/classes/java/main/com/shatteredpixel/shatteredpixeldungeon/ui",
            "core/build/classes/java/main/com/shatteredpixel/shatteredpixeldungeon/utils",

            "core/build/classes/java/main/com/shatteredpixel/shatteredpixeldungeon/Assets.class",
            "core/build/classes/java/main/com/shatteredpixel/shatteredpixeldungeon/Badges.class",
            "core/build/classes/java/main/com/shatteredpixel/shatteredpixeldungeon/Bones.class",
            "core/build/classes/java/main/com/shatteredpixel/shatteredpixeldungeon/Challenges.class",
            "core/build/classes/java/main/com/shatteredpixel/shatteredpixeldungeon/Chrome.class",
            "core/build/classes/java/main/com/shatteredpixel/shatteredpixeldungeon/Dungeon.class",
            "core/build/classes/java/main/com/shatteredpixel/shatteredpixeldungeon/GamesInProgress.class",
            "core/build/classes/java/main/com/shatteredpixel/shatteredpixeldungeon/QuickSlot.class",
            "core/build/classes/java/main/com/shatteredpixel/shatteredpixeldungeon/Rankings.class",
            "core/build/classes/java/main/com/shatteredpixel/shatteredpixeldungeon/ShatteredPixelDungeon.class",
            "core/build/classes/java/main/com/shatteredpixel/shatteredpixeldungeon/Statistics.class"
    };

    public static final String[] keys = {
            "Nanoseconds",
            "Timestamp",
            "Covered Instructions",
            "Total Instructions",
            "Instructions Coverage",
            "Covered Lines",
            "Total Lines",
            "Lines Coverage",
            "Covered Branches",
            "Total Branches",
            "Branches Coverage",
            "Covered Methods",
            "Total Methods",
            "Methods Coverage",
            "Covered Classes",
            "Total Classes",
            "Classes Coverage",
            "Covered Cxty",
            "Total Cxty",
            "Cxty Coverage",
            "Action ID",
            "Action",
            "Env"
    };

    private static JSONObject previousCovData;
    private static JSONObject covDataDiff;

    private static long startTimeStamp = -1;
    private static boolean isFirstRow = true;

    /**
     * Handle the coverage data
     *
     * @param rowNum the row number to start writing the data in the Excel
     * @return the row number to start writing the next data in the Excel
     * @throws IOException   if an I/O error occurs
     * @throws JSONException if an error occurs while parsing the JSON data
     */
    public static int report(int rowNum) throws IOException, JSONException {
        String packageName = generateCoverageReport();
        rowNum = writeExcelFile(rowNum, packageName);

        if (covDataDiff != null) {
            int i = 0;
            for (Iterator it = covDataDiff.keys(); it.hasNext(); ) {
                String key = (String) it.next();

                ReporterClient.totalDiffs.set(i, ReporterClient.totalDiffs.get(i) + covDataDiff.getDouble(key));

                if (ReporterClient.totalDiffs.get(i) >= ReporterClient.DIFF_BOUNDARY) {
                    ReporterClient.stopCounter = 0;
                    ReporterClient.totalDiffs = new ArrayList<>(Arrays.asList(0d,0d,0d,0d,0d,0d));
                    ReporterClient.prevActionCounter = ReporterClient.actionCounter;
                    break;
                }

                ReporterClient.stopCounter += ReporterClient.actionCounter - ReporterClient.prevActionCounter;
                ReporterClient.prevActionCounter = ReporterClient.actionCounter;

                i++;
            }
        }

        return rowNum;
    }


    /**
     * Write the coverage data from the HTML files to the Excel file
     *
     * @param rowNum      the row number to start writing the data in the Excel
     * @param packageName the package name storing the HTML file to fetch the coverage data from
     * @return the row number to start writing the next data in the Excel
     * @throws IOException   if an I/O error occurs
     * @throws JSONException if an error occurs while parsing the JSON data
     */
    public static int writeExcelFile(int rowNum, String packageName) throws IOException, JSONException {
        FileInputStream inputStream = new FileInputStream(EXCEL_FILE_PATH);
        Workbook workbook = new XSSFWorkbook(inputStream);

        Sheet sheet = workbook.getSheet(ReporterClient.PREFIX);

        // Create the sheet if it does not exist
        if (sheet == null) {
            sheet = workbook.createSheet(ReporterClient.PREFIX);
        }

        int cellNum = 0;

        // Clean the sheet if it is the first row;
        // Create the header row if it is the first row
        if (isFirstRow) {
            workbook.removeSheetAt(workbook.getSheetIndex(ReporterClient.PREFIX));
            sheet = workbook.createSheet(ReporterClient.PREFIX);

            Row headerRow = sheet.createRow(0);
            for (String key : keys) {
                Cell cell = headerRow.createCell(cellNum++);
                cell.setCellValue(key);
            }

            isFirstRow = false;
        }

        // Fetch the coverage data and write it to the Excel file
        JSONObject covData = fetchTotalCoverage(packageName);

        // Create the data row
        Row dataRow = sheet.createRow(rowNum++);
        cellNum = 0;

        for (String key : keys) {
            Cell cell = dataRow.createCell(cellNum++);
            if (key.equals("Nanoseconds")) {
                if (startTimeStamp == -1) {
                    startTimeStamp = Long.parseLong(covData.getString("Timestamp"));
                }
                cell.setCellValue(Long.parseLong(covData.getString("Timestamp")) - startTimeStamp);
                continue;
            }

            if (key.equals("Action ID")) {
                cell.setCellValue(ReporterClient.actionCounter);
                continue;
            }

            if (key.equals("Action")) {
                cell.setCellValue(ReporterClient.action);
                continue;
            }

            if (key.equals("Env")) {
                cell.setCellValue(ReporterClient.env);
                continue;
            }

            if (covData.has(key)) {
                if (key.equals("Timestamp")) {
                    cell.setCellValue(Long.parseLong(covData.getString(key)));
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

        printCovData(covData, packageName);

        previousCovData = covData;

        return rowNum;
    }

    private static void printCovData(JSONObject covData, String packageName) throws JSONException {
        GLog.cov("==================================================== Stop Count: " +
                ReporterClient.stopCounter + " / " + ReporterClient.STOP_COUNT +
                " ====================================================");

        String str = "#Action: " + ReporterClient.actionCounter + " - " +
                packageName.split("/")[2] + ": " +
                "Instructions Coverage: " + covData.getDouble("Instructions Coverage") + "%, " +
                "Lines Coverage: " + covData.getDouble("Lines Coverage") + "%, " +
                "Branches Coverage: " + covData.getDouble("Branches Coverage") + "%, " +
                "Methods Coverage: " + covData.getDouble("Methods Coverage") + "%, " +
                "Classes Coverage: " + covData.getDouble("Classes Coverage") + "%, " +
                "Cxty Coverage: " + covData.getDouble("Cxty Coverage") + "%.";

        GLog.cov(str);

        if (previousCovData != null) {
            covDataDiff = new JSONObject();
            covDataDiff.put("Instructions Coverage", covData.getDouble("Instructions Coverage") - previousCovData.getDouble("Instructions Coverage"));
            covDataDiff.put("Lines Coverage", covData.getDouble("Lines Coverage") - previousCovData.getDouble("Lines Coverage"));
            covDataDiff.put("Branches Coverage", covData.getDouble("Branches Coverage") - previousCovData.getDouble("Branches Coverage"));
            covDataDiff.put("Methods Coverage", covData.getDouble("Methods Coverage") - previousCovData.getDouble("Methods Coverage"));
            covDataDiff.put("Classes Coverage", covData.getDouble("Classes Coverage") - previousCovData.getDouble("Classes Coverage"));
            covDataDiff.put("Cxty Coverage", covData.getDouble("Cxty Coverage") - previousCovData.getDouble("Cxty Coverage"));

            str = "Difference: " +
                    "Instructions: " + covDataDiff.getDouble("Instructions Coverage") + "%, " +
                    "Lines: " + covDataDiff.getDouble("Lines Coverage") + "%, " +
                    "Branches: " + covDataDiff.getDouble("Branches Coverage") + "%, " +
                    "Methods: " + covDataDiff.getDouble("Methods Coverage") + "%, " +
                    "Classes: " + covDataDiff.getDouble("Classes Coverage") + "%, " +
                    "Cxty: " + covDataDiff.getDouble("Cxty Coverage") + "%.";

            GLog.cov(str);
        }
    }


    /**
     * Generate the coverage report and save it to the specified directory as exec file
     * and then generate the HTML report
     *
     * @return the package to the generated HTML report
     * @throws IOException if an I/O error occurs
     */
    private static String generateCoverageReport() throws IOException {

        String timestamp = dumpData("");

        String packageName = "";

        try {
            packageName = generateJacocoHtmlReport(CLASS_FILES_DIR, EXEC_FILES_DIR, SOURCE_FILES_DIR, OUT_DIR + "-" + timestamp, ReporterClient.PREFIX + "-" + timestamp + ".exec");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return packageName;
    }

    /**
     * Dump the coverage data
     *
     * @return the timestamp of the coverage data
     * @throws IOException if an I/O error occurs
     */
    public static String dumpDataWithoutCumulating(String pre) throws IOException {
        ExecDumpClient client = new ExecDumpClient();

        client.setDump(true);
        client.setReset(true);

        System.out.println("Connecting to JaCoCo agent...");
        ExecFileLoader loader = client.dump(ADDRESS, PORT);

        List<ExecutionData> executionDataList = (List<ExecutionData>) loader.getExecutionDataStore().getContents();
        List<SessionInfo> sessionInfoList = loader.getSessionInfoStore().getInfos();

        System.out.println("Collected " + executionDataList.size() + " execution data entries.");
        System.out.println("Collected " + sessionInfoList.size() + " session info entries.");

        File reportDir = new File(pre + REPORT_DIR);
        if (!reportDir.exists()) {
            reportDir.mkdirs();
        }

        String timestamp = LocalDateTime.now().format(formatter);

        File reportFile = new File(reportDir, ReporterClient.PREFIX + "-" + timestamp + ".exec");

        try (FileOutputStream fos = new FileOutputStream(reportFile)) {
            loader.save(fos);
        }

        System.out.println("Coverage report saved to " + reportFile.getAbsolutePath());

        return timestamp;
    }


    /*
     *  ======================================================= NEW =======================================================
     */

    public static String dumpData(String pre) throws IOException {
        ExecDumpClient client = new ExecDumpClient();

        client.setDump(true);
        client.setReset(true);

        System.out.println("Connecting to JaCoCo agent...");
        ExecFileLoader loader = client.dump(ADDRESS, PORT);

        List<ExecutionData> executionDataList = (List<ExecutionData>) loader.getExecutionDataStore().getContents();
        List<SessionInfo> sessionInfoList = loader.getSessionInfoStore().getInfos();

        System.out.println("Collected " + executionDataList.size() + " execution data entries.");
        System.out.println("Collected " + sessionInfoList.size() + " session info entries.");

        File reportDir = new File(pre + REPORT_DIR);
        if (!reportDir.exists()) {
            reportDir.mkdirs();
        }

        String timestamp = LocalDateTime.now().format(formatter);

        // Step 1: Save the current coverage data to a new file
        File currentReportFile = new File(reportDir, ReporterClient.PREFIX + "-" + timestamp + ".exec");
        try (FileOutputStream fos = new FileOutputStream(currentReportFile)) {
            loader.save(fos);
        }

        // Step 2: Merge the current data with the latest cumulative data (if it exists)
        ExecFileLoader cumulativeLoader = new ExecFileLoader();
        if (lastReportFile != null) {
            try (FileInputStream fis = new FileInputStream(lastReportFile)) {
                cumulativeLoader.load(fis);
            }
        }

        for (ExecutionData data : loader.getExecutionDataStore().getContents()) {
            cumulativeLoader.getExecutionDataStore().put(data);
        }

        for (SessionInfo info : sessionInfoList) {
            cumulativeLoader.getSessionInfoStore().getInfos().add(info);
        }

        // Step 3: Save the merged data to a new cumulative file
        try (FileOutputStream fos = new FileOutputStream(currentReportFile, true)) {
            cumulativeLoader.save(fos);
        }

        System.out.println("Coverage report saved to " + currentReportFile.getAbsolutePath());

        lastReportFile = currentReportFile;

        return timestamp;
    }

    /**
     * Dump the coverage data, and store the metadata to the metadata file
     *
     * @param actionCounter the action counter
     * @param prevAction    the previous action
     * @param prevEnv       the previous environment
     * @throws IOException   if an I/O error occurs
     * @throws JSONException if an error occurs while parsing the JSON data
     */
    public static void dumpData(int actionCounter, String prevAction, String prevEnv) throws IOException, JSONException {
        String timestamp = dumpData("");

        if (startTimeStamp == -1) {
            startTimeStamp = Long.parseLong(timestamp.split("_")[1]);
        }

        JSONObject msg = new JSONObject();
        msg.put("actionCounter", Integer.toString(actionCounter));
        msg.put("action", prevAction);
        msg.put("env", prevEnv);
        msg.put("timestamp", timestamp.split("_")[1]);
        msg.put("nanoseconds", Long.toString(Long.parseLong(timestamp.split("_")[1]) - startTimeStamp));

        appendToFile(META_DATA_FILE, msg.toString());
    }
}



package com.codecool.dungeoncrawl.agent;

import com.codecool.dungeoncrawl.agent.reporter.JacocoCoverageFetcher;
import com.codecool.dungeoncrawl.agent.reporter.JacocoReportGenerator;
import com.codecool.dungeoncrawl.agent.utils.FileHandler;
import com.codecool.dungeoncrawl.agent.utils.directorHandler;
import com.codecool.dungeoncrawl.agent.reporter.JacocoReporter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.codecool.dungeoncrawl.agent.Reporter.writeCsvFromXML;
import static com.codecool.dungeoncrawl.agent.Reporter.writeExcelFromXML;
import static org.codehaus.plexus.util.FileUtils.mkdir;


public class mergeReporter {

    public static final String[] PREFIXS = {"GADP", "Human", "Monkey", "MonkeyP", "LLMBaseline"};
    public static final String[] SUFFIXS = {"a", "b", "c", ""};
    private static String SUFFIX = SUFFIXS[0];

    public static String[] mergingList1 = new String[]{"Achievement", "Adrenaline", "Aggression", "Caution", "Completion", "Curiosity", "Efficiency"};
    public static String[] mergingList2 = new String[]{"Monkey"};
    public static String[] mergingList3 = new String[]{"MonkeyP"};
    public static String[] mergingList4 = new String[]{"Human"};
    public static String[] mergingList5 = new String[]{"LLMBaseline"};
    public static String[][] mergingLists = new String[][]{mergingList1, mergingList4, mergingList2, mergingList3, mergingList5};

    private static final String ORIG_EXEC_FILES_DIR = "./jacoco-reports/";

    private static final String CLASS_FILES_DIR = "target/classes/com/codecool/dungeoncrawl/logic";
    private static final String EXEC_FILES_DIR = "G:/DA_DATA/jacoco-reports-merged/";
    private static final String SOURCE_FILES_DIR = "src/main/java/com/codecool/dungeoncrawl/logic";
    private static final String ACTION_EXCEL_FILE_PATH = "jacoco-reports/merged_coverage_by_action.xlsx";
    private static final String Time_EXCEL_FILE_PATH = "jacoco-reports/merged_coverage_by_time.xlsx";
    private static final String ACTION_CSV_FILE_PATH = "jacoco-reports/merged_coverage_by_action_";
    private static final String Time_CSV_FILE_PATH = "jacoco-reports/merged_coverage_by_time_";

    private static boolean isFirstRow = true;
    private static int rowNum = 1;

    /**
     * Fetch the directories to be merged
     * @param mergingList The list of prefixes of the directories to be merged
     * @return The list of files to be merged
     * @throws IOException If an I/O error occurs
     */
    private static List<List<String>> getFilesToBeMerged(String[] mergingList) throws IOException {
        List<String> folderNames = directorHandler.fetchDirectoryNames(ORIG_EXEC_FILES_DIR);
        List<String> folderToBeMerged = new ArrayList<>();

        for (String folderName : folderNames) {
            for (String mergeName : mergingList) {
                if (folderName.startsWith(mergeName) && folderName.endsWith(SUFFIX)) {
                    if (mergeName.equals("Monkey") && folderName.startsWith("MonkeyP")) {
                        continue;
                    }
                    folderToBeMerged.add(folderName);
                }
            }
        }

        System.out.println("Directories to be merged: " + folderToBeMerged);

        List<List<String>> filesToBeMerged = new ArrayList<>();

        for (String folderName : folderToBeMerged) {
            List<String> fileNames = directorHandler.fetchFileNames(ORIG_EXEC_FILES_DIR + folderName);
            System.out.println("Files to be merged in " + folderName + ":\n" + fileNames);
            filesToBeMerged.add(fileNames);
        }

        return filesToBeMerged;
    }

    /**
     * Write the merged coverage data to the Excel file
     * @param isByAction Whether the data is by action or by time
     * @param prefix The prefix of the directory
     * @param out_dir The output directory
     * @throws IOException If an I/O error occurs
     * @throws JSONException If a JSON error occurs
     */
    private static void writeExcel(boolean isByAction, String prefix, String out_dir) throws IOException, JSONException {
        FileInputStream inputStream;
        Workbook workbook;
        List<String> folderNames;

        if (isByAction) {
            inputStream = new FileInputStream(ACTION_EXCEL_FILE_PATH);
            workbook = new XSSFWorkbook(inputStream);
            folderNames = directorHandler.fetchDirectoryNames(out_dir + "/By_Action");
        } else {
            inputStream = new FileInputStream(Time_EXCEL_FILE_PATH);
            workbook = new XSSFWorkbook(inputStream);
            folderNames = directorHandler.fetchDirectoryNames(out_dir + "/By_Time");
        }

        System.out.println("Folder names: " + folderNames);

        Sheet sheet = workbook.getSheet(prefix);

        // Create the sheet if it does not exist
        if (sheet == null) {
            sheet = workbook.createSheet(prefix);
        }

        int cellNum = 0;

        // Clean the sheet if it is the first row;
        // Create the header row if it is the first row
        if (isFirstRow) {
            workbook.removeSheetAt(workbook.getSheetIndex(prefix));
            sheet = workbook.createSheet(prefix);

            Row headerRow = sheet.createRow(0);
            for (String key : JacocoReporter.keys) {
                Cell cell = headerRow.createCell(cellNum++);
                cell.setCellValue(key);
            }

            isFirstRow = false;
        }

        String packPre = out_dir + (isByAction ? "/By_Action/" : "/By_Time/");

        for (String folderName : folderNames) {
            // Fetch the coverage data and write it to the Excel file
            JSONObject covData = JacocoCoverageFetcher.fetchTotalCoverage(packPre + folderName);

            // Create the data row
            Row dataRow = sheet.createRow(rowNum++);
            cellNum = 0;

            for (String key : JacocoReporter.keys) {
                Cell cell = dataRow.createCell(cellNum++);

                if (key.equals("Action ID")) {
                    cell.setCellValue(rowNum - 2);
                    continue;
                }

                if (key.equals("Action") || key.equals("Env")) {
                    continue;
                }

                if (isByAction){
                    if (key.equals("Timestamp") || key.equals("Nanoseconds")) {
                        cell.setCellValue(rowNum - 2);
                        continue;
                    }
                } else {
                    if (key.equals("Timestamp") || key.equals("Nanoseconds")) {
                        cell.setCellValue(Long.parseLong(folderName.split("_")[folderName.split("_").length - 1]));
                        continue;
                    }
                }

                cell.setCellValue(covData.getDouble(key));
            }
        }

        // Write the workbook to the file
        try (FileOutputStream fileOut = new FileOutputStream(isByAction ? ACTION_EXCEL_FILE_PATH : Time_EXCEL_FILE_PATH)) {
            workbook.write(fileOut);
        }

        // Closing the workbook
        workbook.close();
    }

    /**
     * Merge the coverage data according to the action number
     * @param prefix The prefix of the directory
     * @param mergingList The list of prefixes of the directories to be merged
     * @param out_dir The output directory
     * @throws IOException If an I/O error occurs
     * @throws InterruptedException If the thread is interrupted
     * @throws JSONException If a JSON error occurs
     */
    private static void mergeByAction(String prefix, String[] mergingList, String out_dir) throws Exception {
        rowNum = 1;
        isFirstRow = true;

        List<List<String>> filesToBeMerged = getFilesToBeMerged(mergingList);
        int totalActions = -1;

        for (List<String> files : filesToBeMerged) {
            totalActions = Math.max(files.size(), totalActions);
        }

        System.out.println("Total actions: " + totalActions);

        // Merge the exec files into a new directory and generate the Jacoco HTML report
        for (int i = 0; i < totalActions; i++) {
            for (List<String> files : filesToBeMerged) {
                if (i < files.size()) {
                    String pre = files.get(i).split("-")[0];

//                    directorHandler.copyFile(
//                            ORIG_EXEC_FILES_DIR + pre + "/" + files.get(i),
//                            EXEC_FILES_DIR + prefix + "/By_Action/" + files.get(i));

                    directorHandler.copyFile(
                            ORIG_EXEC_FILES_DIR + files.get(i).split("-")[0] + "/" + files.get(i),
                            EXEC_FILES_DIR + prefix + "/By_Action/" + SUFFIX + "/" + files.get(i));
                }
            }
            // If the directory exists, skip the generation
            JacocoReportGenerator.generateJacocoXMLReport(CLASS_FILES_DIR, EXEC_FILES_DIR + prefix + "/By_Action/" + SUFFIX,
                    SOURCE_FILES_DIR, out_dir + "/By_Action/" + SUFFIX + "/" + prefix + "_" + i + ".xml");

//            if (!FileHandler.directoryExists(out_dir + "/By_Action/" + prefix + "_" + i)) {
//                JacocoReportGenerator.generateJacocoHtmlReport(CLASS_FILES_DIR, EXEC_FILES_DIR + prefix + "/By_Action", SOURCE_FILES_DIR, out_dir + "/By_Action/" + prefix + "_" + i);
//            } else {
//                System.out.println("Directory " + out_dir + "/By_Action/" + prefix + "_" + i + " already exists. Skipping generation...");
//            }

            // Delete the exec files after merging since all exec files are cumulative
            for (List<String> files : filesToBeMerged) {
                if (i < files.size()) {
                    directorHandler.deleteDirectoryRecursively(Path.of(EXEC_FILES_DIR + prefix + "/By_Action/" + SUFFIX + "/" + files.get(i)));
                }
            }

//            if (i >= 50) { break; }
        }

//        writeExcel(true, prefix, out_dir);

        writeCsvFromXML(prefix, ACTION_CSV_FILE_PATH + prefix + SUFFIX + ".csv", out_dir + "/By_Action/" + SUFFIX + "/", isFirstRow, false, rowNum);

        if (totalActions != -1)
            directorHandler.deleteDirectoryRecursively(Paths.get(EXEC_FILES_DIR + prefix + "/By_Action/" + SUFFIX + "/"));

        mkdir(EXEC_FILES_DIR + prefix + "/By_Action/" + SUFFIX + "/");
    }

    /**
     * Check if the list of files to be merged is empty
     * @param list The list of files to be merged
     * @return Whether the list is empty
     */
    private static boolean isEmpty(List<List<String>> list) {
        for (List<String> files : list) {
            if (!files.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Merge the coverage data according to the time
     * @param prefix The prefix of the directory
     * @param mergingList The list of prefixes of the directories to be merged
     * @param out_dir The output directory
     * @throws IOException If an I/O error occurs
     * @throws InterruptedException If the thread is interrupted
     * @throws JSONException If a JSON error occurs
     */
    private static void mergeByTime(String prefix, String[] mergingList, String out_dir) throws Exception {
        rowNum = 1;
        isFirstRow = true;

        List<List<String>> filesToBeMerged = getFilesToBeMerged(mergingList);
        List<Long> fileStartTimes = new ArrayList<>();

        for (List<String> files : filesToBeMerged) {
            fileStartTimes.add(Long.valueOf(files.get(0).split("_")[files.get(0).split("_").length - 1].replace(".exec", "")));
        }

        System.out.println("File start times: " + fileStartTimes);

        int stopCounter = 0;

        // Merge the exec files into a new directory and generate the Jacoco HTML report
//        while(!isEmpty(filesToBeMerged) && stopCounter < 50) {
        while(!isEmpty(filesToBeMerged)) {
            List<Integer> earliestIndex = new ArrayList<>();
            long earliestTime = Long.MAX_VALUE;

            // Find the earliest time, if there are multiple files with the same time, merge them all
            for (int i = 0; i < fileStartTimes.size(); i++) {
                List<String> files = filesToBeMerged.get(i);
                if (files.isEmpty()) {
                    continue;
                }
                Long time = Long.valueOf(files.get(0).split("_")[files.get(0).split("_").length - 1].replace(".exec", ""));

                if (!files.isEmpty() && (time - fileStartTimes.get(i)) < earliestTime) {
                    earliestTime = time - fileStartTimes.get(i);
                    earliestIndex.clear();
                    earliestIndex.add(i);

                } else if (!files.isEmpty() && (time - fileStartTimes.get(i)) == earliestTime) {
                    earliestTime = time - fileStartTimes.get(i);
                    earliestIndex.add(i);
                }
            }

            for (int i : earliestIndex) {
                String pre = filesToBeMerged.get(i).get(0).split("-")[0];

                FileHandler.deleteFilesStartWith(EXEC_FILES_DIR + prefix + "/By_Time/" + SUFFIX + "/", pre);

                directorHandler.copyFile(
                        ORIG_EXEC_FILES_DIR + pre + "/" + filesToBeMerged.get(i).get(0),
                        EXEC_FILES_DIR + prefix + "/By_Time/" + SUFFIX + "/" + filesToBeMerged.get(i).get(0));
            }

            // If the directory exists, skip the generation
            JacocoReportGenerator.generateJacocoXMLReport(CLASS_FILES_DIR, EXEC_FILES_DIR + prefix + "/By_Time/" + SUFFIX,
                    SOURCE_FILES_DIR, out_dir + "/By_Time/" + SUFFIX + "/" + prefix + "_" + earliestTime + ".xml");

//            if (!FileHandler.directoryExists(out_dir + "/By_Time/" + prefix + "_" + earliestTime)) {
//                JacocoReportGenerator.generateJacocoHtmlReport(CLASS_FILES_DIR, EXEC_FILES_DIR + prefix + "/By_Time", SOURCE_FILES_DIR, out_dir + "/By_Time/" + prefix + "_" + earliestTime);
//            } else {
//                System.out.println("Directory " + out_dir + "/By_Time/" + prefix + "_" + earliestTime + " already exists. Skipping generation...");
//            }

            for (int i : earliestIndex) {
                filesToBeMerged.get(i).remove(0);
            }

            stopCounter++;
        }

        writeCsvFromXML(prefix, Time_CSV_FILE_PATH + prefix + SUFFIX + ".csv", out_dir + "/By_Time/" + SUFFIX + "/", isFirstRow, false, rowNum);

        if (!isEmpty(filesToBeMerged))
            directorHandler.deleteDirectoryRecursively(Paths.get(EXEC_FILES_DIR + prefix + "/By_Time/" + SUFFIX + "/"));

        mkdir(EXEC_FILES_DIR + prefix + "/By_Time/" + SUFFIX + "/");
    }

    public static void main(String[] args) throws Exception {
        for (int i = 3; i < 4; i++) {
            for (int j = 0; j < 3; j++) {
                String prefix = PREFIXS[i];
                SUFFIX = SUFFIXS[j];
                String[] mergingList = mergingLists[i];
                String out_dir = "G:\\DA_DATA/jacoco-reports-xml-merged/" + prefix;

//                mergeByAction(prefix, mergingList, out_dir);
                mergeByTime(prefix, mergingList, out_dir);
            }
        }

//        JacocoReportGenerator.generateJacocoHtmlReport(CLASS_FILES_DIR, "D:\\McGill\\Graduated Study\\Generative_Agents\\Codes\\GADP_Dungeon_Advantures\\draft\\Human/", SOURCE_FILES_DIR, "G:/DA_DATA/Draft/Human", "Human2-2024-11-22-19-57-53-083_71873083899000.exec");
//        JacocoReportGenerator.generateJacocoHtmlReport(CLASS_FILES_DIR, "D:\\McGill\\Graduated Study\\Generative_Agents\\Codes\\GADP_Dungeon_Advantures\\draft\\Agent/", SOURCE_FILES_DIR, "G:/DA_DATA/Draft/Agent", "CuriosityHybrid3-2024-11-08-20-34-02-024_5682445495900.exec");
    }
}

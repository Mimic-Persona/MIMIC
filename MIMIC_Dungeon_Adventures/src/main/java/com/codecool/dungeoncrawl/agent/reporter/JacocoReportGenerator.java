package com.codecool.dungeoncrawl.agent.reporter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JacocoReportGenerator {
    private static final String JACOCO_CLI_JAR_PATH = "./lib/jacococli.jar"; // Update this with the actual path to jacococli.jar

    public static String generateJacocoHtmlReport(String classFilesDir, String execFilesDir, String sourceFilesDir, String outputDir) throws IOException, InterruptedException {

        System.out.println("Generating JaCoCo HTML report...");

        ProcessBuilder processBuilder = new ProcessBuilder(
                "java",
                "-jar",
                JACOCO_CLI_JAR_PATH,
                "report",
                execFilesDir + "/*.exec",
                "--classfiles", classFilesDir,
                "--sourcefiles", sourceFilesDir,
                "--html", outputDir
        );

        processBuilder.inheritIO();
        Process process = processBuilder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("JaCoCo report generation failed with exit code " + exitCode);
        } else {
            System.out.println("JaCoCo HTML report for " + outputDir + " generated successfully with exit code: " + exitCode);
        }

        return outputDir;
    }

    public static String generateJacocoHtmlReport(String classFilesDir, String execFilesDir, String sourceFilesDir, String outputDir, String execFile) throws IOException, InterruptedException {

        System.out.println("Generating JaCoCo HTML report...");

        ProcessBuilder processBuilder = new ProcessBuilder(
                "java",
                "-jar",
                JACOCO_CLI_JAR_PATH,
                "report",
                execFilesDir + execFile,
                "--classfiles", classFilesDir,
                "--sourcefiles", sourceFilesDir,
                "--html", outputDir
        );

        processBuilder.inheritIO();
        Process process = processBuilder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("JaCoCo report generation failed with exit code " + exitCode);
        } else {
            System.out.println("JaCoCo HTML report for " + outputDir + " generated successfully with exit code: " + exitCode);
        }

        return outputDir;
    }

    public static String generateJacocoHtmlReport(String classFilesDir, List<String> execFiles, String sourceFilesDir, String outputDir) throws IOException, InterruptedException {

        System.out.println("Generating JaCoCo HTML report...");

        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar");
        command.add(JACOCO_CLI_JAR_PATH);
        command.add("report");

        // Add each exec file to the command list
        command.addAll(execFiles);

        // Add other necessary arguments
        command.add("--classfiles");
        command.add(classFilesDir);
        command.add("--sourcefiles");
        command.add(sourceFilesDir);
        command.add("--html");
        command.add(outputDir);

        // Initialize ProcessBuilder
        ProcessBuilder processBuilder = new ProcessBuilder(command);

        processBuilder.inheritIO();
        Process process = processBuilder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("JaCoCo report generation failed with exit code " + exitCode);
        } else {
            System.out.println("JaCoCo HTML report for " + outputDir + " generated successfully with exit code: " + exitCode);
        }

        return outputDir;
    }

    public static String generateJacocoXMLReport(String classFilesDir, String execFilesDir, String sourceFilesDir, String outputDir) throws IOException, InterruptedException {

        // Create output directory if it does not exist
        File directory = new File(outputDir.substring(0, outputDir.lastIndexOf("/")));
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File file = new File(outputDir);

        if (file.exists()) {
            System.out.println("JaCoCo XML report " + outputDir + " is already generated. Skipping...");
            return outputDir;
        }

        System.out.println("Generating JaCoCo XML report...");

        ProcessBuilder processBuilder = new ProcessBuilder(
                "java",
                "-jar",
                JACOCO_CLI_JAR_PATH,
                "report",
                execFilesDir + "/*.exec",
                "--classfiles", classFilesDir,
                "--sourcefiles", sourceFilesDir,
                "--xml", outputDir
        );

        processBuilder.inheritIO();
        Process process = processBuilder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("JaCoCo report generation failed with exit code " + exitCode);
        } else {
            System.out.println("JaCoCo XML report for " + outputDir + " generated successfully with exit code: " + exitCode);
        }

        return outputDir;
    }

    public static String generateJacocoXMLReport(String classFilesDir, String execFilesDir, String sourceFilesDir, String outputDir, String execFile) throws IOException, InterruptedException {

        // Create output directory if it does not exist
        File directory = new File(outputDir.substring(0, outputDir.lastIndexOf("/")));
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File file = new File(outputDir);

        if (file.exists()) {
            System.out.println("JaCoCo XML report " + outputDir + " is already generated. Skipping...");
            return outputDir;
        }

        System.out.println("Generating JaCoCo XML report...");

        ProcessBuilder processBuilder = new ProcessBuilder(
                "java",
                "-jar",
                JACOCO_CLI_JAR_PATH,
                "report",
                execFilesDir + "/" + execFile,
                "--classfiles", classFilesDir,
                "--sourcefiles", sourceFilesDir,
                "--xml", outputDir
        );

        processBuilder.inheritIO();
        Process process = processBuilder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("JaCoCo report generation failed with exit code " + exitCode);
        } else {
            System.out.println("JaCoCo XML report for " + outputDir + " generated successfully with exit code: " + exitCode);
        }

        return outputDir;
    }

    private static String getExecFiles(String execFilesDir) {
        File dir = new File(execFilesDir);
        File[] execFiles = dir.listFiles((d, name) -> name.endsWith(".exec"));

        if (execFiles == null || execFiles.length == 0) {
            throw new RuntimeException("No .exec files found in " + execFilesDir);
        }

        StringBuilder execFilesArg = new StringBuilder();
        for (File execFile : execFiles) {
            execFilesArg.append(execFile.getAbsolutePath()).append(" ");
        }

        return execFilesArg.toString().trim();
    }
}

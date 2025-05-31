package com.codecool.dungeoncrawl.agent;

import java.io.File;

import static com.codecool.dungeoncrawl.agent.mergeReporter.SUFFIXS;

public class PrefixFormatter {

    public static void prefixFormatter(String dirPath) {
        // Get the directory name
        File dir = new File(dirPath);
        String directoryName = dir.getName();

        // Get all the files in the directory
        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    // Get the original file name
                    String originalFileName = file.getName();

                    // Split the file name at the first hyphen
                    String[] nameParts = originalFileName.split("-", 2);

                    // Ensure the file has a prefix and a suffix
                    if (nameParts.length == 2) {
                        // Form the new file name using the directory name as the prefix
                        String newFileName = directoryName + "-" + nameParts[1];

                        // Create a new File object for the renamed file
                        File renamedFile = new File(file.getParent(), newFileName);

                        // Rename the file
                        if (file.renameTo(renamedFile)) {
                            System.out.println("Renamed: " + originalFileName + " -> " + newFileName);
                        } else {
                            System.out.println("Failed to rename: " + originalFileName);
                        }
                    } else {
                        System.out.println("File doesn't match the pattern: " + originalFileName);
                    }
                }
            }
        } else {
            System.out.println("The directory is empty or invalid.");
        }
    }


    public static void main(String[] args) {
        // Specify the directory path
        String rootDir = "G:/DA_DATA/jacoco-reports/";

        // Call the prefixFormatter method
        for (int i = 1; i <= 7; i++) {
            for (int j = 0; j < 3; j++) {
                String dirPath = rootDir + "Monkey" + i + SUFFIXS[j] + "/";
                prefixFormatter(dirPath);
            }
        }


    }
}

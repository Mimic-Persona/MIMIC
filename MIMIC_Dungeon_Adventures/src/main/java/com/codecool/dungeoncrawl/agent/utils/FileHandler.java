package com.codecool.dungeoncrawl.agent.utils;

import com.codecool.dungeoncrawl.agent.GLog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class FileHandler {

    // Method to write to a file (overwrite if exists)
    public static void writeFile(String filePath, String content) {
        File file = new File(filePath);
        try {
            // Create directories if they don't exist
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            // Create file if it doesn't exist
            if (!file.exists()) {
                file.createNewFile();
            }
            // Write content to the file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(content);
            }

            System.out.println("Log file written successfully.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to append to a file
    public static void appendToFile(String filePath, String content) {
        File file = new File(filePath);
        try {
            // Create directories if they don't exist
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            // Create file if it doesn't exist
            if (!file.exists()) {
                file.createNewFile();
            }
            // Append content to the file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                writer.write(content);
                writer.newLine(); // Add a new line after the appended content
            }

            System.out.println("Log file appended successfully.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteFilesStartWith(String path, String pre) {
        List<String> names = directorHandler.fetchFileNames(path);

        for (String name : names) {
            if (name.startsWith(pre)) {
                File file = new File(path + name);
                file.delete();
            }
        }

    }

    public static boolean directoryExists(String dir) {
        File file = new File(dir);
        return file.exists();
    }
}

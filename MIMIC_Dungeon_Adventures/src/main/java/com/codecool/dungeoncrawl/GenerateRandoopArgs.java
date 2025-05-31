package com.codecool.dungeoncrawl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class GenerateRandoopArgs {
    public static void main(String[] args) {
        Path directoryPath = Paths.get("./src/main/java/com/codecool/dungeoncrawl/logic/");
        List<String> classNames = getClassNames(directoryPath);
        classNames.forEach(System.out::println);

        String fileName = "./src/main/java/com/codecool/dungeoncrawl/myClasses.txt";

        try {
            FileWriter fileWriter = new FileWriter(fileName);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            for (String className : classNames) {
                bufferedWriter.write("com.codecool.dungeoncrawl.logic" + className + "\n");
            }

            // Closing resources
            bufferedWriter.close();
            fileWriter.close();

            System.out.println("Successfully wrote to the file: " + fileName);

        } catch (IOException e) {
            System.err.println("Error writing to the file: " + e.getMessage());
        }
    }

    public static List<String> getClassNames(Path directoryPath) {
        List<String> classNames = new ArrayList<>();
        File directory = directoryPath.toFile();

        if (!directory.exists()) {
            System.out.println("Directory does not exist: " + directoryPath);
            return classNames;
        }

        // Recursive function to fetch class names
        fetchClassNames(directory, classNames, ".");
        return classNames;
    }

    private static void fetchClassNames(File directory, List<String> classNames, String prefix) {
        // List all files in the directory
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // Recursively fetch class names from subdirectory
                    fetchClassNames(file, classNames, prefix + file.getName() + ".");
                } else if (file.isFile() && file.getName().endsWith(".java")) {
                    // Found a .class file, add its name to classNames list
                    String className = file.getName().replace(".java", "");
                    classNames.add(prefix + className);
                }
            }
        }
    }
}

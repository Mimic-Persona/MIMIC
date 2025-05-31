package com.shatteredpixel.shatteredpixeldungeon.agent.utils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class directorHandler {

    /**
     * Fetches the names of the files in the specified directory
     * @param directoryPath the path to the directory
     * @return a list of file names
     */
    public static List<String> fetchFileNames(String directoryPath) {
        List<String> fileNames = new ArrayList<>();
        Path path = Paths.get(directoryPath);

        if (!Files.isDirectory(path)) {
            System.err.println(directoryPath + " is not a valid directory.");
            return fileNames;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry)) {
                    fileNames.add(entry.getFileName().toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        fileNames.sort(new DirectoryNameComparator());

        return fileNames;
    }

    /**
     * Fetches the names of the directories in the specified directory
     * @param directoryPath the path to the directory
     * @return a list of directory names
     * @throws IOException if an I/O error occurs
     */
    public static List<String> fetchDirectoryNames(String directoryPath) throws IOException {
        List<String> directoryNames = new ArrayList<>();
        Path path = Paths.get(directoryPath);

        if (!Files.isDirectory(path)) {
            System.err.println(directoryPath + " is not a valid directory.");
            return directoryNames;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    directoryNames.add(entry.getFileName().toString());
                }
            }
        }

        directoryNames.sort(new DirectoryNameComparator());

        return directoryNames;
    }

    public static void copyFile(String source, String destination) {
        try {
            Path dest = Paths.get(destination);

            if (!Files.exists(dest.getParent())) {
                Files.createDirectories(dest.getParent());
            }

            Files.copy(Paths.get(source), dest);

        } catch (IOException e) {
            System.out.println("Failed to copy " + source + " to " + destination);
        }
    }

    public static void deleteDirectoryRecursively(Path path) throws IOException {
        // Walk through the directory tree and delete each file/subdirectory
        Files.walk(path)
                .sorted(Comparator.reverseOrder()) // Sort in reverse order so files are deleted before directories
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to delete " + p, e);
                    }
                });
    }
}

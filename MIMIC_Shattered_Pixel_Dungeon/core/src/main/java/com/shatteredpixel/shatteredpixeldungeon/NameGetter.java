package com.shatteredpixel.shatteredpixeldungeon;

import java.io.File;

public class NameGetter {
    // Get the name of all the classes by fetching the file name given the directory
    public static String getName(String dir) {
        String[] split = dir.split("/");
        String name = split[split.length - 1];
        return name.substring(0, name.length() - 5);
    }

    public static void main(String[] args) {
        // Get the name of all the classes in the directory if it is a directory, nest directories to get the names of all the classes
        File directory = new File("core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/buffs/");
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                File[] subFiles = file.listFiles();
                for (File subFile : subFiles) {
                    if (subFile.isFile()) {
                        System.out.print("\"" + getName(subFile.getName()) + "\", ");
                    }
                }
            } else if (file.isFile()) {
                System.out.print("\"" +  getName(file.getName()) + "\", ");
            }
        }
    }
}

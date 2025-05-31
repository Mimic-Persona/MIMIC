package com.codecool.dungeoncrawl.agent.utils;

import java.util.Comparator;

public class DirectoryNameComparator implements Comparator<String> {
    @Override
    public int compare(String o1, String o2) {
        o1 = o1.replace(".txt", "").replace(".exec", "").replace(".xml", "");
        o2 = o2.replace(".txt", "").replace(".exec", "").replace(".xml", "");

        if (!o1.contains("_") || !o2.contains("_")) {
            return o1.compareTo(o2);
        }

        if (o1.split("-").length >= 4) {
            int date1 = Integer.parseInt(o1.split("-")[3]);
            int date2 = Integer.parseInt(o2.split("-")[3]);

            if (date1 != date2) {
                return Integer.compare(date1, date2);
            }
        }

        long index1 = Long.parseLong(o1.split("_")[o1.split("_").length - 1]);
        long index2 = Long.parseLong(o2.split("_")[o2.split("_").length - 1]);

        return Long.compare(index1, index2);
    }
}

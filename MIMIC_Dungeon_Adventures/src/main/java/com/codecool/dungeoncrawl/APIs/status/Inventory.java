package com.codecool.dungeoncrawl.APIs.status;

import com.codecool.dungeoncrawl.logic.items.*;

import static com.codecool.dungeoncrawl.gui.Main.map;

public class Inventory {
    public static int getNumOfKeys() {
        int num = 0;

        for (Item item : map.getPlayer().getItems()) {
            if (item instanceof Key) {
                num++;
            }
        }

        return num;
    }

    public static int getNumOfAxes() {
        int num = 0;

        for (Item item : map.getPlayer().getItems()) {
            if (item instanceof Axe) {
                num++;
            }
        }

        return num;
    }

    public static int getNumOfShields() {
        int num = 0;

        for (Item item : map.getPlayer().getItems()) {
            if (item instanceof Shield) {
                num++;
            }
        }

        return num;
    }

    public static int getNumOfHealthPotions() {
        int num = 0;

        for (Item item : map.getPlayer().getItems()) {
            if (item instanceof HealthPotion) {
                num++;
            }
        }

        return num;
    }

}

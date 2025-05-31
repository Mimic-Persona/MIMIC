package com.codecool.dungeoncrawl.APIs.status;

import com.codecool.dungeoncrawl.logic.map.Cell;

import static com.codecool.dungeoncrawl.gui.Main.map;

public class Position {

    public static Cell getHeroPosition() {
        return map.getPlayer().getCell();
    }
}

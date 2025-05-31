package com.codecool.dungeoncrawl.logic.items;


import com.codecool.dungeoncrawl.logic.map.Cell;

public class Shield extends Item {
    public Shield(Cell cell) {
        super(cell);
        setHealth(10);
    }

    @Override
    public String getTileName() {
        return "shield";
    }
}

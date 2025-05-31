package com.codecool.dungeoncrawl.logic.items;


import com.codecool.dungeoncrawl.logic.map.Cell;

public class Key extends Item {
    public Key(Cell cell) {
        super(cell);
    }
    @Override
    public String getTileName() {
        return "key";
    }
}

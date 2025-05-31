package com.codecool.dungeoncrawl.logic.items;

import com.codecool.dungeoncrawl.logic.map.Cell;

public class HealthPotion extends Item{
    public HealthPotion(Cell cell) {
        super(cell);
        setHealth(15);
    }

    @Override
    public String getTileName() {
        return "potion";
    }
}

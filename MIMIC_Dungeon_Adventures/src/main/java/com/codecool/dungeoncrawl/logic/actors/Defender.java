package com.codecool.dungeoncrawl.logic.actors;

import com.codecool.dungeoncrawl.logic.map.Cell;

public class Defender extends Actor {

    public Defender(Cell cell) {
        super(cell);
        health = 70;
        damage = 7;
    }

    @Override
    public void move() {

    }


    @Override
    public String getTileName() {
        return "defender";
    }
}

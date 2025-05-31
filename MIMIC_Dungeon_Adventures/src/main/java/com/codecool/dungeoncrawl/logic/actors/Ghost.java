package com.codecool.dungeoncrawl.logic.actors;

import com.codecool.dungeoncrawl.logic.map.Cell;
import com.codecool.dungeoncrawl.logic.map.CellType;
import com.codecool.dungeoncrawl.logic.map.GameMap;

import java.util.Random;

public class Ghost extends Actor {
    Random random = new Random();
    private static final double TELEPORT_PROBABILITY = 0.2;

    public Ghost(Cell cell) {
        super(cell);
        health = 30;
        damage = 3;
    }

    public void move(){
        if (isReadyToTeleport()) teleport();
        while(true){
            int[][] coordsDifferentials = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
            int[] diff = coordsDifferentials[random.nextInt(coordsDifferentials.length)];
            if (cell.hasNeighbor(diff[0], diff[1])
                    && cell.getNeighbor(diff[0], diff[1]).getActor() == null){
                    //&& !(cell.getNeighbor(diff[0], diff[1]).getActor()  instanceof Player)) {
                changeCell(diff[0], diff[1]);
                break;
            }
            if (cell.hasNeighbor(diff[0], diff[1]) && cell.getActor() != null){
                break;
            }
        }
    }

    // teleports on a cell which is of type FLOOR and doesn't contain actor nor item
    private void teleport() {
        GameMap map = cell.getGameMap();
        int width = map.getWidth();
        int height = map.getHeight();
        int x, y;
        do {
            x = random.nextInt(width);
            y = random.nextInt(height);
        } while (map.getCell(x, y).getActor() != null
                || map.getCell(x, y).getItem() != null
                || !CellType.FLOOR.equals(map.getCell(x, y).getType()));
        changeCell(x - cell.getX(), y - cell.getY());
    }

    // returns true with probability = TELEPORT_PROBABILITY
    private boolean isReadyToTeleport() {
        return random.nextInt((int) Math.floor((1 / TELEPORT_PROBABILITY))) == 0;
    }

    @Override
    public String getTileName() {
        return "ghost";
    }
}

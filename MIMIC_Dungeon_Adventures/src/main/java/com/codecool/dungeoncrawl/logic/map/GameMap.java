package com.codecool.dungeoncrawl.logic.map;

import com.codecool.dungeoncrawl.agent.skill_library.basic_skills.Utils;
import com.codecool.dungeoncrawl.agent.GLog;
import com.codecool.dungeoncrawl.gui.Main;
import com.codecool.dungeoncrawl.logic.actors.Actor;
import com.codecool.dungeoncrawl.logic.actors.Player;

import java.util.*;

public class GameMap {
    private int width;
    private int height;

    private Main main;
    private Cell[][] cells;
    private final List<CellType> obstacles = Arrays.asList(CellType.WALL, CellType.CLOSE);
    private final List<CellType> walls = Arrays.asList(CellType.WALL);
    private Player player;

    public GameMap(Main main, int width, int height, CellType defaultCellType) {
        this.main = main;
        this.width = width;
        this.height = height;
        cells = new Cell[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cells[x][y] = new Cell(this, x, y, defaultCellType);
            }
        }
    }

    public void clearCells() {
        for (Cell[] row : cells) {
            for (Cell cell : row) {
                cell.clear();
            }
        }
    }

    public Actor getNearestMob() {
        List<Actor> mobs = getMobs();
        Actor nearestMob = null;
        int minDistance = Integer.MAX_VALUE;

        for (Actor mob: mobs){
            int distance = Utils.getDistance(player.getCell(), mob.getCell());
            if (distance < minDistance){
                minDistance = distance;
                nearestMob = mob;
            }
        }

        return nearestMob;
    }

    public Cell getCell(int x, int y) {
        if (x < 0 || x >= cells.length || y < 0 || y >= cells[0].length) {
            return null;
        }
        return cells[x][y];
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public List<CellType> getObstacles() {
        return obstacles;
    }

    public List<CellType> getWalls() {
        return walls;
    }

    public List<Actor> getMobs() {
        List<Actor> mobs = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (cells[x][y].getActor() != null
                        && ! (cells[x][y].getActor() instanceof Player)) {
                    mobs.add(cells[x][y].getActor());
                }
            }
        }
        return mobs;
    }

    public void removeDeadMobs(){
        List<Actor> mobs = getMobs();
        for (Actor mob: mobs){
            if (mob.getHealth() <= 0) {
                mob.getCell().setActor(null);
                GLog.h(mob.toString() + " is dead.");
            };
        }
    }

     public List<CellType> getO(){
        return obstacles;
    }

    public Main getMain() {
        return main;
    }

    public boolean isPlayerOnCoords(int x, int y){
        return player.getX() == x && player.getY() == y;
    }

    public boolean areCoordsOnMap(int i, int j) {
        return i >= 0 && i < width && j >= 0 && j < height;
    }

    public Cell[][] getCells() {
        return cells;
    }

    @Override
    public String toString() {
        return Arrays.deepToString(getCells());
    }
}

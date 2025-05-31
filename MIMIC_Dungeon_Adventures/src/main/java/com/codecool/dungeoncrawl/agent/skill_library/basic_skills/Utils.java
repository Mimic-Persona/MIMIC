package com.codecool.dungeoncrawl.agent.skill_library.basic_skills;

import com.codecool.dungeoncrawl.logic.map.Cell;
import com.codecool.dungeoncrawl.logic.map.CellType;

import static com.codecool.dungeoncrawl.gui.Main.map;
import static com.codecool.dungeoncrawl.logic.actors.Player.NAMES;

public class Utils {
    public static boolean isValid(Cell cell) {
        return cell.getX() >= 0 && cell.getX() < map.getWidth() && cell.getY() >= 0 && cell.getY() < map.getHeight() &&
                cell.getType() != CellType.EMPTY &&
                cell.getType() != CellType.WALL;
    }

    public static boolean isValid(int x, int y) {
        return x >= 0 && x < map.getWidth() && y >= 0 && y < map.getHeight() &&
                map.getCell(x, y).getType() != CellType.EMPTY &&
                map.getCell(x, y).getType() != CellType.WALL;
    }

    public static boolean isWall(Cell cell) {
        return map.getWalls().contains(cell.getType()) && !NAMES.contains(map.getPlayer().getName());
    }

    public static boolean isWall(int x, int y) {
        return map.getWalls().contains(map.getCell(x, y).getType()) && !NAMES.contains(map.getPlayer().getName());
    }

    public static boolean isObstacle(Cell cell) {
        return map.getObstacles().contains(cell.getType()) && !NAMES.contains(map.getPlayer().getName());
    }

    public static boolean isObstacle(int x, int y) {
        return map.getObstacles().contains(map.getCell(x, y).getType()) && !NAMES.contains(map.getPlayer().getName());
    }

    public static boolean isMob(Cell cell) {
        return cell.getActor() != null;
    }

    public static boolean isMob(int x, int y) {
        return map.getCell(x, y).getActor() != null;
    }

    public static int getDistance(Cell a, Cell b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }
}

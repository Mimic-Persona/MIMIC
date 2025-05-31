package com.codecool.dungeoncrawl.agent.skill_library.basic_skills;

import com.codecool.dungeoncrawl.agent.GLog;
import com.codecool.dungeoncrawl.logic.map.Cell;
import com.codecool.dungeoncrawl.logic.map.CellType;
import javafx.application.Platform;

import java.util.*;
import java.util.concurrent.CountDownLatch;

import static com.codecool.dungeoncrawl.agent.skill_library.basic_skills.Utils.*;
import static com.codecool.dungeoncrawl.gui.Main.map;

public class Navigate {
    private static final int[][] DIRECTIONS = {
            {0, 1}, {1, 0}, {0, -1}, {-1, 0} // Right, Down, Left, Up
    };

    public static boolean navigate(int x, int y) {
        Cell end = map.getCell(x, y);
        return navigate(end);
    }

    public static boolean navigate(Cell end) {
        Cell start = map.getPlayer().getCell();
        List<Cell> path = findPathIgnoreMobs(end);

        if (path.isEmpty()) {
            GLog.e("basic_skill.Navigate:error: No path found from " + start.getCellPosStr() + " to " + end.getCellPosStr() + ".");
            return false;
        }

        boolean flag = true;

        while (flag) {
            flag = false;

            for (int i = 1; i < path.size(); i++) {
                Cell next = path.get(i);

                // Refresh the game
                // Create a CountDownLatch with the count of 1
                CountDownLatch latch = new CountDownLatch(1);

                Platform.runLater(() -> {
                    // Refresh the game
                    map.getMain().refresh();

                    // Count down the latch after game is refreshed
                    latch.countDown();
                });

                try {
                    // Wait for the latch to reach zero
                    latch.await();
                } catch (InterruptedException e) {
                    GLog.e("basic_skill.Navigate:error: Error in waiting for latch: " + e.getMessage());
                    Thread.currentThread().interrupt();
                    return false;
                }

                // If the mob is alive on the next cell, reconstruct the path
                if (next.getActor() == null || next.getActor().getHealth() > 0) {
                    map.getPlayer().move(next.getX() - map.getPlayer().getX(), next.getY() - map.getPlayer().getY());
                }

                // If the player is blocked by a closed door, try once and stop
                if (next.getType().equals(CellType.CLOSE)) {
                    GLog.e("basic_skill.Navigate:error: No path found from " + start.getCellPosStr() + " to " + end.getCellPosStr() + ".");
                    return false;
                }

                // If the player is not at the next cell, reconstruct the path
                if (next.getActor() == null) {
                    path = findPathIgnoreMobs(end);
                    flag = true;
                    break;
                }

            }
        }

        return true;
    }

    public static boolean navigateIgnoreMobs(int x, int y) {
        Cell end = map.getCell(x, y);
        return navigateIgnoreMobs(end);
    }

    public static boolean navigateIgnoreMobs(Cell end) {
        Cell start = map.getPlayer().getCell();
        List<Cell> path = findPathIgnoreMobs(end);

        if (path.isEmpty()) {
            GLog.e("basic_skill.Navigate:error: No path found from " + start.getCellPosStr() + " to " + end.getCellPosStr() + ".");
            return false;
        }

        for (int i = 1; i < path.size(); i++) {
            Cell next = path.get(i);

            // Try until the player reaches the next cell
            while (map.getPlayer().getCell().getX() != next.getX() || map.getPlayer().getCell().getY() != next.getY()) {
                // Create a CountDownLatch with the count of 1
                CountDownLatch latch = new CountDownLatch(1);

                Platform.runLater(() -> {
                    // Refresh the game
                    map.getMain().refresh();

                    // Count down the latch after game is refreshed
                    latch.countDown();
                });

                try {
                    // Wait for the latch to reach zero
                    latch.await();
                } catch (InterruptedException e) {
                    GLog.e("basic_skill.Navigate:error: Error in waiting for latch: " + e.getMessage());
                    Thread.currentThread().interrupt();
                    return false;
                }

                // If the mob on the next cell is dead, refresh the game
                if (next.getActor() == null || next.getActor().getHealth() > 0)
                    map.getPlayer().move(next.getX() - map.getPlayer().getX(), next.getY() - map.getPlayer().getY());

                // If the player is blocked by a closed door, try once and stop
                if (next.getType().equals(CellType.CLOSE)) return false;
            }
        }

        return true;
    }

    public static List<Cell> findPathIgnoreMobs(Cell end) {
        if (!isValid(end) || isWall(end) || end == null) {
            return Collections.emptyList();
        }

        Cell start = map.getPlayer().getCell();
        map.clearCells();

        Queue<Cell> queue = new LinkedList<>();
        boolean[][] visited = new boolean[map.getWidth()][map.getHeight()];
        queue.add(start);
        visited[start.getX()][start.getY()] = true;

        while (!queue.isEmpty()) {
            Cell current = queue.poll();

            // If we've reached the end node, reconstruct the path
            if (current.getX() == end.getX() && current.getY() == end.getY()) {
                return reconstructPath(current);
            }

            for (int[] direction : DIRECTIONS) {
                int newX = current.getX() + direction[0];
                int newY = current.getY() + direction[1];

                if (isValid(newX, newY) && !visited[newX][newY] && !isWall(newX, newY)) {
                    visited[newX][newY] = true;

                    Cell newCell = map.getCell(newX, newY);
                    newCell.parent = current;
                    queue.add(newCell);
                }
            }
        }

        return Collections.emptyList(); // No path found
    }

    private static List<Cell> reconstructPath(Cell end) {
        List<Cell> path = new ArrayList<>();
        for (Cell cell = end; cell != null; cell = cell.parent) {
            path.add(cell);
        }
        Collections.reverse(path);
        return path;
    }
}


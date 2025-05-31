package com.codecool.dungeoncrawl.logic.actors;

import com.codecool.dungeoncrawl.logic.Objects.Stairs;
import com.codecool.dungeoncrawl.logic.map.Cell;
import com.codecool.dungeoncrawl.logic.map.CellType;
import com.codecool.dungeoncrawl.logic.map.GameMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Boss extends Actor{
    Random random = new Random();
    private static final List<CellType> GROUND = List.of(CellType.FLOOR);

    public Boss(Cell cell) {
        super(cell);
        health = 200;
        damage = 10;
    }

    @Override
    public void move() {
        GameMap map = cell.getGameMap();
        Player player = map.getPlayer();
        Cell playerCell = player.cell;

        List<int[]> steps = getTrackingSteps();
        List<int[]> filteredSteps = steps.stream().filter(d -> cell.hasNeighbor(d[0], d[1]))
                .filter(d -> GROUND.contains(cell.getNeighbor(d[0], d[1]).getType()))
                .filter(d -> playerCell.equals(cell.getNeighbor(d[0], d[1])) || cell.getNeighbor(d[0], d[1]).getActor() == null)
                .collect(Collectors.toList());

        if(filteredSteps.size() > 0){
            int[] step = filteredSteps.get(random.nextInt(filteredSteps.size()));
            Cell next = cell.getNeighbor(step[0], step[1]);
            if (next.getActor() == null)  changeCell(step[0], step[1]);
            else {
                fight(player);
                if (health <= 0) cell.getGameMap().getMain().nextLevel();
            }
        }
    }



    private List<int[]> getTrackingSteps() {
        int searchRadius = 20; //radius in the sense of chess metric
        int x = cell.getX();
        int y = cell.getY();
        for (int i = x - searchRadius; i <= x + searchRadius ; i++) {
            for (int j = y - searchRadius; j <= y + searchRadius; j++) {
                if (cell.getGameMap().areCoordsOnMap(i, j) && cell.getGameMap().isPlayerOnCoords(i, j)){
                    int dx = (int) Math.signum(i - x);
                    int dy = (int) Math.signum(j - y);
                    if (dx * dy == 0) return List.of(new int[] {dx, dy});
                    else return List.of(new int[]{dx, 0}, new int[] {0, dy});
                }
            }
        }
        return new ArrayList<>();
    }

    @Override
    public String getTileName() {
        return "boss";
    }
}

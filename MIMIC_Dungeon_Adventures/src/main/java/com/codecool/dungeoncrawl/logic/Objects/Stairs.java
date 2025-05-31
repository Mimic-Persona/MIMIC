package com.codecool.dungeoncrawl.logic.Objects;

import com.codecool.dungeoncrawl.agent.GLog;
import com.codecool.dungeoncrawl.gui.Main;
import com.codecool.dungeoncrawl.logic.items.Item;
import com.codecool.dungeoncrawl.logic.items.Key;
import com.codecool.dungeoncrawl.logic.map.Cell;
import com.codecool.dungeoncrawl.logic.map.CellType;
import java.util.Objects;

public class Stairs {
    public static void goDown(int dx, int dy, Cell cell) {
        Cell ogject = cell.getGameMap().getCell(cell.getGameMap().getPlayer().getX() + dx, cell.getGameMap().getPlayer().getY() + dy);
        if (Objects.equals(ogject.getType(), CellType.STAIRSDOWN )){
            cell.getGameMap().getMain().nextLevel();
            GLog.h("You went down to the next level.");
        }
        if (Objects.equals(ogject.getType(), CellType.STAIRSUP )){
            cell.getGameMap().getMain().previousLevel();
            GLog.h("You went up to the previous level.");
        }
    }
}

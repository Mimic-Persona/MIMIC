package com.codecool.dungeoncrawl.logic.Objects;

import com.codecool.dungeoncrawl.agent.GLog;
import com.codecool.dungeoncrawl.gui.Main;
import com.codecool.dungeoncrawl.logic.items.Item;
import com.codecool.dungeoncrawl.logic.items.Key;
import com.codecool.dungeoncrawl.logic.map.Cell;
import com.codecool.dungeoncrawl.logic.map.CellType;
import com.codecool.dungeoncrawl.logic.map.GameMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class Door {

    public static boolean tryOpen(int dx, int dy, GameMap map, ArrayList<Item> items) {
        Cell object = map.getCell(map.getPlayer().getX() + dx, map.getPlayer().getY() + dy);

        if (Objects.equals(object.getType(),CellType.CLOSE )){
            Iterator<Item> it = items.iterator();

            while (it.hasNext()){
                Item i = it.next();
                if(i instanceof Key){
                    object.setType(CellType.OPEN);
                    it.remove();
                    GLog.h("You opened the door at [" + object.getX() + ", " + object.getY() + "]." );
                    return true;
                }
            }
            GLog.h("You need a key to open the door at [" + object.getX() + ", " + object.getY() + "]." );
            return false;
        }

        return true;
    }
}

package com.codecool.dungeoncrawl.logic.actors;

import com.codecool.dungeoncrawl.APIs.status.Environment;
import com.codecool.dungeoncrawl.APIs.status.Status;
import com.codecool.dungeoncrawl.agent.GLog;
import com.codecool.dungeoncrawl.gui.Main;
import com.codecool.dungeoncrawl.logic.Objects.WinObject;
import com.codecool.dungeoncrawl.logic.Objects.Door;
import com.codecool.dungeoncrawl.logic.Objects.Stairs;
import com.codecool.dungeoncrawl.logic.map.Cell;
import com.codecool.dungeoncrawl.logic.map.GameMap;
import com.codecool.dungeoncrawl.logic.items.Item;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import static com.codecool.dungeoncrawl.gui.Main.map;
import static com.codecool.dungeoncrawl.gui.Main.prevAction;


public class Player extends Actor {
    String name = "player";
    public static final List<String> NAMES = List.of("admin");

    ArrayList<Item> items = new ArrayList<>();

    public Player(Cell cell) {
        super(cell);
        health = 300;
        damage = 20;
        items = new ArrayList<>();
    }

    public void move(int dx, int dy) {
        Main.actionCounter++;
        GLog.c("======================================================================== Action #" + Main.actionCounter + " ========================================================================");

        int newX = cell.getX() + dx;
        int newY = cell.getY() + dy;

        Main.prevAction = "move (" + newX + ", " + newY + ")";
        if (Environment.getTile(newX, newY) != null) {
            Main.prevAction += " " + Environment.getTile(newX, newY).getType().toString();
        } else {
            Main.prevAction += " NONE";
        }

        if (Environment.getTile(newX, newY) != null && Environment.getTile(newX, newY).getItem() != null) {
            Main.prevAction += " " + Environment.getTile(newX, newY).getItem().toString();
        }

        try {
            Main.prevEnv = Status.getStatus().toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Main.toReport = true;

        GameMap map = cell.getGameMap();
        if (!cell.hasNeighbor(dx, dy)) return;
        Cell neighbor = cell.getNeighbor(dx, dy);

        // check we have key if yes open door
        Door.tryOpen(dx, dy, map, items);
        Stairs.goDown(dx, dy, cell);
        WinObject.checkWin(dx, dy, cell);

        // check object is in collide list
        if (map.getObstacles().contains(neighbor.getType()) && !NAMES.contains(name)) {
            return;
        }

        // check is enemies
        if (neighbor.getActor() != null) {
            Actor enemy = neighbor.getActor();
            prevAction = "attack (" + this.getCell().getX() + ", " + this.getCell().getY() + ") " + enemy.getCell().getActor().toString();
            fight(enemy);

        } else {
            Cell oldCell = cell;
            changeCell(dx, dy); // move

            GLog.h("Moved from " + oldCell.getCellPosStr() + " to " + cell.getCellPosStr() + ".");
        }
    }

    public ArrayList<Item> getInventory() {
        return items;
    }

    public String getTileName() {
        return "player";
    }

    @Override
    public void move() {
        Main.actionCounter++;
        Main.toReport = true;
    }

    public boolean pickUpItem(){
        Main.actionCounter++;
        GLog.c("======================================================================== Action #" + Main.actionCounter + " ========================================================================");

        Main.prevAction = "pickup (" + this.getCell().getX() + ", " + this.getCell().getY() + ") ";
        if (cell.getItem() != null)
            Main.prevAction += cell.getItem().toString();

        try {
            Main.prevEnv = Status.getStatus().toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Main.toReport = true;

        if (cell.getItem() == null) {
            GLog.h("There is no item to pick up on " + map.getPlayer().getCell().getCellPosStr() + ".");
            return false;
        }

        items.add(cell.getItem());
        GLog.h("Item " + cell.getItem().toString() + " is picked up.");

        damage += cell.getItem().getDamage();
        GLog.h("Damage increased by " + cell.getItem().getDamage() + ".");

        health += cell.getItem().getHealth();
        GLog.h("Health increased by " + cell.getItem().getHealth() + ".");

        cell.setItem(null);

        return true;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setAttributes(ArrayList<Item> items,int health,int damage,String name){
        this.health = health;
        this.damage = damage;
        this.items = items;
        this.name = name;
    }

    public ArrayList<Item> getItems() {
        return new ArrayList<>(items);
    }
}

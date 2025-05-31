package com.codecool.dungeoncrawl.logic.actors;

import com.codecool.dungeoncrawl.agent.GLog;
import com.codecool.dungeoncrawl.logic.map.Cell;
import com.codecool.dungeoncrawl.logic.Drawable;

public abstract class Actor implements Drawable {
    Cell cell;
    int health;
    int damage ;

    public Actor(Cell cell) {
        this.cell = cell;
        this.cell.setActor(this);
    }

    public void changeCell(int dx, int dy) {
        Cell nextCell = cell.getNeighbor(dx, dy);
        cell.setActor(null);
        nextCell.setActor(this);
        cell = nextCell;
    }

    public void attack(Actor attacker, Actor defender){
        defender.health -= damage;
        GLog.h(attacker.toString() + " deals " + damage + " damage to " + defender.toString());
    }

    public void fight(Actor enemy) {
        GLog.h(this.toString() + " attack " + enemy.toString());
        if (health > 0) attack(this, enemy);
        if (enemy.health > 0) enemy.attack(enemy, this);
    }

    public abstract void move();

    public int getHealth() {
        return health;
    }

    public int getDamage() {
        return damage;
    }

    public Cell getCell() {
        return cell;
    }

    public int getX() {
        return cell.getX();
    }

    public int getY() {
        return cell.getY();
    }

    public void setCell(Cell cell) {
        this.cell = cell;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " ([" + cell.getX() + ", " + cell.getY() + "], " + health + " HP, " + damage + " Damage)";
    }
}

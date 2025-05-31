package com.codecool.dungeoncrawl.agent.skill_library.basic_skills;

import com.codecool.dungeoncrawl.agent.GLog;
import com.codecool.dungeoncrawl.logic.actors.Actor;
import com.codecool.dungeoncrawl.logic.map.Cell;
import com.codecool.dungeoncrawl.logic.map.CellType;

import java.util.List;

import static com.codecool.dungeoncrawl.agent.skill_library.basic_skills.Navigate.*;
import static com.codecool.dungeoncrawl.gui.Main.map;

public class KillMob {
    public static boolean killMob(int x, int y) {
        Cell cell = map.getCell(x, y);
        return killMob(cell.getActor());
    }

    public static boolean killMob(Actor mob) {
        if (mob == null || !map.getMobs().contains(mob) || mob.getHealth() <= 0) {
            GLog.e("basic_skill.KillMob:error: The mob is not valid.");
            return false;
        }

        while (mob.getHealth() > 0 && map.getPlayer().getHealth() > 0) {
            List<Cell> path = findPathIgnoreMobs(mob.getCell());

            if (path.isEmpty()) {
                GLog.e("basic_skill.KillMob:error: The mob is unreachable.");
                return false;
            }

            Cell next = path.get(1);
            map.getPlayer().move(next.getX() - map.getPlayer().getX(), next.getY() - map.getPlayer().getY());

            // If the player is blocked by a closed door, try once and stop
            if (next.getType().equals(CellType.CLOSE)) return false;
        }
        return true;
    }
}


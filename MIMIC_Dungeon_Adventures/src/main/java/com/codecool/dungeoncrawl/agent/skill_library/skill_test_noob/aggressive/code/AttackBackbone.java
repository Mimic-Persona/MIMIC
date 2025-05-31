package com.codecool.dungeoncrawl.agent.skill_library.skill_test_noob.aggressive.code;

import com.codecool.dungeoncrawl.agent.GLog;

import com.codecool.dungeoncrawl.logic.actors.Actor;
import com.codecool.dungeoncrawl.logic.actors.Backbone;
import com.codecool.dungeoncrawl.logic.actors.Boss;
import com.codecool.dungeoncrawl.logic.actors.Defender;
import com.codecool.dungeoncrawl.logic.actors.Ghost;
import com.codecool.dungeoncrawl.logic.actors.Player;

import com.codecool.dungeoncrawl.logic.items.Axe;
import com.codecool.dungeoncrawl.logic.items.HealthPotion;
import com.codecool.dungeoncrawl.logic.items.Item;
import com.codecool.dungeoncrawl.logic.items.Key;
import com.codecool.dungeoncrawl.logic.items.Shield;

import com.codecool.dungeoncrawl.logic.map.Cell;
import com.codecool.dungeoncrawl.logic.map.CellType;
import com.codecool.dungeoncrawl.logic.map.GameMap;

import com.codecool.dungeoncrawl.logic.Objects.Door;
import com.codecool.dungeoncrawl.logic.Objects.Stairs;
import com.codecool.dungeoncrawl.logic.Objects.WinObject;

import com.codecool.dungeoncrawl.agent.skill_library.basic_skills.KillMob;
import com.codecool.dungeoncrawl.agent.skill_library.basic_skills.Navigate;
import com.codecool.dungeoncrawl.agent.skill_library.basic_skills.PickUp;
import com.codecool.dungeoncrawl.agent.skill_library.basic_skills.Utils;

import static com.codecool.dungeoncrawl.gui.Main.map;


public class AttackBackbone {
    public static void attackBackbone() {
        // Coordinates of the Backbone mob
        int mobX = 23;
        int mobY = 19;

        // Log the start of the attack
        GLog.h("Starting to attack the Backbone at (" + mobX + ", " + mobY + ").");

        // Use the killMob method to attack the Backbone
        boolean success = KillMob.killMob(mobX, mobY);

        // Log the result of the attack
        if (success) {
            GLog.h("Successfully killed the Backbone at (" + mobX + ", " + mobY + ").");
        } else {
            GLog.h("Failed to kill the Backbone at (" + mobX + ", " + mobY + ").");
        }
    }

    public static void main(String[] args) {
        attackBackbone(); // call your function here
    }
}
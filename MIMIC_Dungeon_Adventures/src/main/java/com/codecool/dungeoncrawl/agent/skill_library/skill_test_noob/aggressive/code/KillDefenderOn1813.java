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

import com.codecool.dungeoncrawl.agent.skill_library.skill_test_noob.aggressive.code.KillDefenderOn1813;
import com.codecool.dungeoncrawl.agent.skill_library.skill_test_noob.aggressive.code.AttackBackbone;
import com.codecool.dungeoncrawl.agent.skill_library.skill_test_noob.aggressive.code.KillGhost;
import com.codecool.dungeoncrawl.agent.skill_library.skill_test_noob.aggressive.code.KillGhostOn2115;

public class KillDefenderOn1813 {
    public static void killDefenderOn1813() {
        // Step 1: Navigate to the key at [16, 13] and pick it up
        GLog.h("Navigating to the key at (16, 13).");
        boolean reachedKey = Navigate.navigateIgnoreMobs(map.getCell(16, 13));
        if (reachedKey) {
            GLog.h("Picking up the key at (16, 13).");
            boolean pickedKey = PickUp.pickUp();
            if (!pickedKey) {
                GLog.h("Failed to pick up the key at (16, 13).");
                return;
            }
        } else {
            GLog.h("Failed to reach the key at (16, 13).");
            return;
        }

        // Step 2: Navigate to the Defender at [18, 13]
        GLog.h("Navigating to the Defender at (18, 13).");
        boolean reachedDefender = Navigate.navigateIgnoreMobs(map.getCell(18, 13));
        if (!reachedDefender) {
            GLog.h("Failed to reach the Defender at (18, 13).");
            return;
        }

        // Step 3: Kill the Defender
        GLog.h("Starting to attack the Defender at (18, 13).");
        boolean success = KillMob.killMob(18, 13);
        if (success) {
            GLog.h("Successfully killed the Defender at (18, 13).");
        } else {
            GLog.h("Failed to kill the Defender at (18, 13).");
            return;
        }

        // Step 4: Pick up the HealthPotion dropped by the Defender
        GLog.h("Picking up the HealthPotion at (18, 13).");
        boolean pickedPotion = PickUp.pickUp();
        if (pickedPotion) {
            GLog.h("Successfully picked up the HealthPotion at (18, 13).");
        } else {
            GLog.h("Failed to pick up the HealthPotion at (18, 13).");
        }
    }

    public static void main(String[] args) {
        killDefenderOn1813(); // call your function here
    }
}
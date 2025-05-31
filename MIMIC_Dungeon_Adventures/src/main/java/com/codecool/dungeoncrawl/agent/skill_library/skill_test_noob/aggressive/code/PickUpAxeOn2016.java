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

import com.codecool.dungeoncrawl.agent.skill_library.skill_test_noob.aggressive.code.PickUpAxeOn2016;
import com.codecool.dungeoncrawl.agent.skill_library.skill_test_noob.aggressive.code.KillGhostOn2115;
import com.codecool.dungeoncrawl.agent.skill_library.skill_test_noob.aggressive.code.AttackBackbone;
import com.codecool.dungeoncrawl.agent.skill_library.skill_test_noob.aggressive.code.KillGhost;

public class PickUpAxeOn2016 {
    public static void pickUpAxeOn2016() {
        // Step 1: Navigate to the axe at [20, 16]
        GLog.h("Navigating to the axe at (20, 16).");
        boolean reachedAxe = Navigate.navigateIgnoreMobs(map.getCell(20, 16));
        if (!reachedAxe) {
            GLog.h("Failed to reach the axe at (20, 16).");
            return;
        }

        // Step 2: Pick up the axe
        GLog.h("Picking up the axe at (20, 16).");
        boolean pickedUp = PickUp.pickUp();
        if (pickedUp) {
            GLog.h("Successfully picked up the axe at (20, 16).");
        } else {
            GLog.h("Failed to pick up the axe at (20, 16).");
        }
    }

    public static void main(String[] args) {
        pickUpAxeOn2016(); // call your function here
    }
}
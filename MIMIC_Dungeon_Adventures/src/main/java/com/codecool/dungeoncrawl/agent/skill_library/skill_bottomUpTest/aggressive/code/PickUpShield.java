package com.codecool.dungeoncrawl.agent.skill_library.skill_bottomUpTest.aggressive.code;

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

import com.codecool.dungeoncrawl.agent.skill_library.skill_bottomUpTest.aggressive.code.PickUpKey;
import com.codecool.dungeoncrawl.agent.skill_library.skill_bottomUpTest.aggressive.code.PickUpHealthPotion;
import com.codecool.dungeoncrawl.agent.skill_library.skill_bottomUpTest.aggressive.code.PickUpHealthPotion;
import com.codecool.dungeoncrawl.agent.skill_library.skill_bottomUpTest.aggressive.code.KillDefender;

public class PickUpShield {
    public static void pickUpShield(int shieldX, int shieldY) {
        // Log the start of the task
        GLog.h("Starting to pick up the shield at position [" + shieldX + ", " + shieldY + "]");

        // Check if the shield is present at the specified location
        Cell shieldCell = map.getCell(shieldX, shieldY);
        if (shieldCell.getItem() == null || !shieldCell.getItem().getName().equals("Shield")) {
            GLog.h("No shield found at position [" + shieldX + ", " + shieldY + "]");
            return;
        }

        // Navigate to the shield's position
        boolean navigated = Navigate.navigateIgnoreMobs(shieldCell);

        // If navigation is successful, pick up the shield
        if (navigated) {
            boolean pickedUp = PickUp.pickUp();
            if (pickedUp) {
                GLog.h("Successfully picked up the shield at position [" + shieldX + ", " + shieldY + "]");
            } else {
                GLog.h("Failed to pick up the shield at position [" + shieldX + ", " + shieldY + "]");
            }
        } else {
            GLog.h("Failed to navigate to the shield at position [" + shieldX + ", " + shieldY + "]");
        }
    }

    public static void main(String[] args) {
        pickUpShield(22, 5); // Call the function with the given position
    }
}
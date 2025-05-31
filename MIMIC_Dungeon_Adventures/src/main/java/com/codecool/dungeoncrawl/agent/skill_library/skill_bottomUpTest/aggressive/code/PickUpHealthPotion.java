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

import com.codecool.dungeoncrawl.agent.skill_library.skill_bottomUpTest.aggressive.code.PickUpHealthPotion;
import com.codecool.dungeoncrawl.agent.skill_library.skill_bottomUpTest.aggressive.code.PickUpAxe;
import com.codecool.dungeoncrawl.agent.skill_library.skill_bottomUpTest.aggressive.code.PickUpShield;
import com.codecool.dungeoncrawl.agent.skill_library.skill_bottomUpTest.aggressive.code.PickUpKey;

public class PickUpHealthPotion {
    public static void pickUpHealthPotion(int potionX, int potionY) {
        // Log the start of the task
        GLog.h("Starting to pick up the Health Potion at position [" + potionX + ", " + potionY + "]");

        // Check if the health potion is present at the specified location
        Cell potionCell = map.getCell(potionX, potionY);
        if (potionCell.getItem() == null || !potionCell.getItem().getName().equals("Health Potion")) {
            GLog.h("No Health Potion found at position [" + potionX + ", " + potionY + "]");
            return;
        }

        // Navigate to the health potion's position
        boolean navigated = Navigate.navigateIgnoreMobs(potionCell);

        // If navigation is successful, pick up the health potion
        if (navigated) {
            boolean pickedUp = PickUp.pickUp();
            if (pickedUp) {
                GLog.h("Successfully picked up the Health Potion at position [" + potionX + ", " + potionY + "]");
            } else {
                GLog.h("Failed to pick up the Health Potion at position [" + potionX + ", " + potionY + "]");
            }
        } else {
            GLog.h("Failed to navigate to the Health Potion at position [" + potionX + ", " + potionY + "]");
        }
    }

    public static void main(String[] args) {
        pickUpHealthPotion(18, 13); // Call the function with the given position
    }
}
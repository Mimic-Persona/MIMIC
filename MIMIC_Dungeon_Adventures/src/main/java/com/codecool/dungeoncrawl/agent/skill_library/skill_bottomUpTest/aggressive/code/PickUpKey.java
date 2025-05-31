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
import com.codecool.dungeoncrawl.agent.skill_library.skill_bottomUpTest.aggressive.code.KillDefender;
import com.codecool.dungeoncrawl.agent.skill_library.skill_bottomUpTest.aggressive.code.AttackBackbone;

public class PickUpKey {
    public static void pickUpKey(int keyX, int keyY) {
        // Log the start of the task
        GLog.h("Starting to pick up the key at position [" + keyX + ", " + keyY + "]");

        // Kill the Ghost at the key's position
        boolean killed = KillMob.killMob(keyX, keyY);

        // If the Ghost is successfully killed, navigate to the key's position
        if (killed) {
            boolean navigated = Navigate.navigateIgnoreMobs(map.getCell(keyX, keyY));

            // If navigation is successful, pick up the key
            if (navigated) {
                boolean pickedUp = PickUp.pickUp();
                if (pickedUp) {
                    GLog.h("Successfully picked up the key at position [" + keyX + ", " + keyY + "]");
                } else {
                    GLog.h("Failed to pick up the key at position [" + keyX + ", " + keyY + "]");
                }
            } else {
                GLog.h("Failed to navigate to the key at position [" + keyX + ", " + keyY + "]");
            }
        } else {
            GLog.h("Failed to kill the Ghost at position [" + keyX + ", " + keyY + "]");
        }
    }

    public static void main(String[] args) {
        pickUpKey(16, 13); // Call the function with the given position
    }
}
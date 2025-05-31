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

import com.codecool.dungeoncrawl.agent.skill_library.skill_bottomUpTest.aggressive.code.MoveToPosition;
import com.codecool.dungeoncrawl.agent.skill_library.skill_bottomUpTest.aggressive.code.PickUpKey;
import com.codecool.dungeoncrawl.agent.skill_library.skill_bottomUpTest.aggressive.code.PickUpAxe;
import com.codecool.dungeoncrawl.agent.skill_library.skill_bottomUpTest.aggressive.code.PickUpShield;

public class MoveToPosition {
    public static void moveToPosition(int x, int y) {
        // Log the start of the task
        GLog.h("Starting to move to position [" + x + ", " + y + "]");

        // Check if the target position is valid
        if (!Utils.isValid(x, y)) {
            GLog.h("Invalid target position [" + x + ", " + y + "]");
            return;
        }

        // Navigate to the target position
        boolean navigated = Navigate.navigateIgnoreMobs(map.getCell(x, y));

        // Log the success or failure of the navigation
        if (navigated) {
            GLog.h("Successfully moved to position [" + x + ", " + y + "]");
        } else {
            GLog.h("Failed to move to position [" + x + ", " + y + "]");
        }
    }

    public static void main(String[] args) {
        moveToPosition(20, 21); // Call the function with the given position
    }
}
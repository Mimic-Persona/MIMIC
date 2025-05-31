package com.codecool.dungeoncrawl.agent.monkey;

import com.codecool.dungeoncrawl.gui.Main;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

import static com.codecool.dungeoncrawl.agent.utils.JsonUtils.jsonFileToDictionary;
import static com.codecool.dungeoncrawl.gui.Main.map;

public class MonkeyAPI {

    // TODO: Define the constants before running the experiment
    public static final int MAX_ACTIONS = Integer.MAX_VALUE; // Define the maximum number of actions for the monkey

    public static JSONObject config = jsonFileToDictionary("./MIMIC_Dungeon_Adventures/config.json");
    public static final boolean WITH_PRECONDITION; // Define whether the monkey should run with preconditions

    static {
        assert config != null;
        try {
            WITH_PRECONDITION = config.getBoolean("IS_SMART_MONKEY");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Handle the smart monkey's action.
     */
    public static void handle() {
        Main.isAgentNext = false;
        String action;

        if (WITH_PRECONDITION) {
            action = pickActionWithPre();
        } else {
            action = pickAction();
        }

        if (action.equals("move")) monkeyMove();
        else monkeyPickUp();
    }

    /**
     * Pick an action without preconditions between "move" and "pickup";
     * actions will be picked with equal probability.
     * @return the picked action
     */
    private static String pickAction() {
        // Define the weight for "move" and "pickup"
        double moveWeight = 0.5;

        // Create a Random object
        Random random = new Random();

        // Generate a random number between 0 and 1
        double randomValue = random.nextDouble();

        // Determine the action based on the generated random number
        String randomAction;
        if (randomValue < moveWeight) {
            randomAction = "move";
        } else {
            randomAction = "pickup";
        }

        return randomAction;
    }


    /**
     * Pick an action with preconditions between "move" and "pickup";
     * 100% pickup if is on an item; 50% pickup if is not on an item.
     * @return the picked action
     */
    private static String pickActionWithPre() {
        if (map.getPlayer().getCell().getItem() != null) return "pickup";

        // Define the weight for "move" and "pickup"
        double moveWeight = 0.5;

        // Create a Random object
        Random random = new Random();

        // Generate a random number between 0 and 1
        double randomValue = random.nextDouble();

        // Determine the action based on the generated random number
        String randomAction;
        if (randomValue < moveWeight) {
            randomAction = "move";
        } else {
            randomAction = "pickup";
        }

        return randomAction;
    }

    /**
     * Move the monkey to a random adjacent tile.
     */
    private static void monkeyMove() {
        int[][] nextTiles = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        Random random = new Random();

        // Randomly select a direction
        int[] selectedTile = nextTiles[random.nextInt(nextTiles.length)];

        // Move to the selected tile
        map.getPlayer().move(selectedTile[0], selectedTile[1]);
    }

    /**
     * Pick up the item on the current tile.
     */
    private static void monkeyPickUp() {
        map.getPlayer().pickUpItem();
    }
}

package com.codecool.dungeoncrawl.APIs.status;

import com.codecool.dungeoncrawl.gui.Main;
import org.json.JSONException;
import org.json.JSONObject;

import static com.codecool.dungeoncrawl.gui.Main.map;

public class Status {

    public static int getHeroHealth() {
        return map.getPlayer().getHealth();
    }

    public static int getHeroDamage() {
        return map.getPlayer().getDamage();
    }

    /**
     * Get the status of the hero in the form of a JSON object
     * @return The status of the hero
     * @throws JSONException The exception thrown when there is an error in the JSON object
     */
    public static JSONObject getStatus() throws JSONException {
        JSONObject result = new JSONObject();

        // Inventory
        result.put("keys", Inventory.getNumOfKeys());
        result.put("Axes", Inventory.getNumOfAxes());
        result.put("Shields", Inventory.getNumOfShields());
        result.put("HealthPotions", Inventory.getNumOfHealthPotions());

        // Position
        result.put("heroPositionInXY", Position.getHeroPosition().getCellPosStr());
        result.put("dungeonLevel", Main.level);

        // Hero Status
        result.put("health", getHeroHealth());
        result.put("damage", getHeroDamage());

        // Environment
        result.put("environment", Environment.getImportantCellsJSON());

        return result;
    }
}

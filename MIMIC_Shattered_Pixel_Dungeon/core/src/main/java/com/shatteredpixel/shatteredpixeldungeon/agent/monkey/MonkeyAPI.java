package com.shatteredpixel.shatteredpixeldungeon.agent.monkey;

import com.shatteredpixel.shatteredpixeldungeon.APIs.status.Position;
import com.shatteredpixel.shatteredpixeldungeon.APIs.status.Status;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.shatteredpixel.shatteredpixeldungeon.APIs.AgentAPI.*;
import static com.shatteredpixel.shatteredpixeldungeon.APIs.status.Environment.getMapSize;
import static com.shatteredpixel.shatteredpixeldungeon.agent.utils.JsonUtils.jsonFileToDictionary;

public class MonkeyAPI {

    // TODO: Define the constants before running the experiment
    public static final int MAX_ACTIONS = Integer.MAX_VALUE; // Define the maximum number of actions for the monkey

    public static JSONObject config = jsonFileToDictionary("../config.json");
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
        Dungeon.isAgentNext = false;
        String action = WITH_PRECONDITION ? pickActionWithPre() : pickAction();

        switch (action) {
            case "act":
                monkeyAct();
                break;
            case "wait":
                monkeyWait();
                break;
            case "equip":
                monkeyEquip();
                break;
            case "unequip":
                monkeyUnequip();
                break;
            case "drop":
                monkeyDrop();
                break;
            case "use":
                monkeyUse();
                break;
            case "throw":
                monkeyThrow();
                break;
            default:
                monkeyUpgrade();
                break;
        }
    }

    /**
     * Pick an action with preconditions between "act", "wait", "equip", "unequip", "drop", "use", "throw", and "upgrade";
     * "act" : No preconditions
     * "wait" : No preconditions
     * "equip" : Must have at least one equipable item
     * "unequip" : Must have at least one equipped item
     * "drop" : Must have at least one item
     * "use" : Must have at least one item
     * "throw" : Must have at least one item
     * "upgrade" : Must have at least one talent
     *
     * @return the picked action
     */
    private static String pickActionWithPre() {
        List<String> actions = new ArrayList<>();
        actions.add("act");
        actions.add("wait");

        if (!Dungeon.hero.belongings.getAllItems(EquipableItem.class).isEmpty()) {
            actions.add("equip");
        }

        if (!Dungeon.hero.belongings.getEquipments().isEmpty()) {
            actions.add("unequip");
        }

        if (!Dungeon.hero.belongings.getAllItems(Item.class).isEmpty()) {
            actions.add("drop");
            actions.add("use");
            actions.add("throw");
        }

        // Check if talent available
        boolean unspentTalents = false;
        for (int i = 1; i <= Dungeon.hero.talents.size(); i++){
            if (Dungeon.hero.talentPointsAvailable(i) > 0){
                unspentTalents = true;
                break;
            }
        }

        if (unspentTalents) {
            actions.add("upgrade");
        }

        // Create a Random object
        Random random = new Random();

        // Generate a random index
        int index = random.nextInt(actions.size());

        return actions.get(index);
    }

    /**
     * Pick an action randomly without preconditions between "act", "wait", "equip", "unequip", "drop", "use", "throw", and "upgrade".
     * @return the picked action
     */
    private static String pickAction() {
        String[] actions = {"act", "wait", "equip", "unequip", "drop", "use", "throw", "upgrade"};

        // Create a Random object
        Random random = new Random();

        // Generate a random index
        int index = random.nextInt(actions.length);

        return actions[index];
    }

    /**
     * Let the monkey act to a random adjacent tile.
     */
    private static void monkeyAct() {
        int[][] nextTiles = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}, {0, 0}};
        Random random = new Random();

        // Randomly select a direction
        int[] selectedDirection = nextTiles[random.nextInt(nextTiles.length)];

        int selectedX = Position.getHeroPositionXY()[0] + selectedDirection[0];
        int selectedY = Position.getHeroPositionXY()[1] + selectedDirection[1];

        // Move to the selected tile
        act(new int[]{selectedX, selectedY});
    }

    /**
     * Let the monkey wait.
     */
    private static void monkeyWait() {
        heroWait();
    }

    /**
     * Let the monkey equip a random equipable item in the belongings.
     */
    private static void monkeyEquip() {
        ArrayList<EquipableItem> items = Dungeon.hero.belongings.getAllItems(EquipableItem.class);

        Random random = new Random();

        // Randomly select an item
        try {
        EquipableItem selectedItem = items.get(random.nextInt(items.size()));

        equip(selectedItem.name());

        } catch (IllegalArgumentException ignored) {}
    }

    /**
     * Let the monkey unequip a random equipped item.
     */
    private static void monkeyUnequip() {
        ArrayList<EquipableItem> equipments = Dungeon.hero.belongings.getEquipments();

        Random random = new Random();

        // Randomly select an item
        try {
            EquipableItem selectedItem = equipments.get(random.nextInt(equipments.size()));

            unEquip(selectedItem.name());

        } catch (IllegalArgumentException ignored) {}
    }

    /**
     * Let the monkey drop a random item in the belongings.
     */
    private static void monkeyDrop() {
        ArrayList<Item> items = Dungeon.hero.belongings.getAllItems(Item.class);

        Random random = new Random();

        // Randomly select an item
        try {
            Item selectedItem = items.get(random.nextInt(items.size()));

            drop(selectedItem.name());

        } catch (IllegalArgumentException ignored) {}
    }

    /**
     * Let the monkey use a random item in the belongings.
     */
    private static void monkeyUse() {
        ArrayList<Item> items = Dungeon.hero.belongings.getAllItems(Item.class);

        Random random = new Random();

        // Randomly select an item
        try {
            Item selectedItem = items.get(random.nextInt(items.size()));
            Item selectedItem2 = items.get(random.nextInt(items.size()));
            int pos = random.nextInt(getMapSize());

            use(Position.transferPos2XY(pos), selectedItem.name(), selectedItem2.name());

        } catch (IllegalArgumentException ignored) {}
    }

    /**
     * Let the monkey throw a random item in the belongings to a random tile.
     */
    private static void monkeyThrow() {
        ArrayList<Item> items = Dungeon.hero.belongings.getAllItems(Item.class);

        Random random = new Random();

        int pos = random.nextInt(getMapSize());

        // Randomly select an item
        try {
            Item selectedItem = items.get(random.nextInt(items.size()));

            throwItem(pos, selectedItem.name());

        } catch (IllegalArgumentException ignored) {}
    }

    /**
     * Let the monkey upgrade a random talent.
     */
    private static void monkeyUpgrade() {
        ArrayList<ArrayList<Talent>> allTalents = Status.getHeroTalents();

        Random random = new Random();

        try {
            ArrayList<Talent> selectedTier = allTalents.get(random.nextInt(allTalents.size()));
            Talent selectedTalent = selectedTier.get(random.nextInt(selectedTier.size()));

            upgrade(selectedTalent.name());

        } catch (IllegalArgumentException ignored) {}
    }

}

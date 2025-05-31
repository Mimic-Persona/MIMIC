package com.shatteredpixel.shatteredpixeldungeon.APIs.status;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Inventory {
    public Inventory() {}

    public static int getHeroInventorySize() {
        return 20;
    }

    public static ArrayList<EquipableItem> getEquipments() {
        ArrayList<EquipableItem> equipments = new ArrayList<>();

        equipments.add(Dungeon.hero.belongings.weapon);
        equipments.add(Dungeon.hero.belongings.armor);
        equipments.add(Dungeon.hero.belongings.artifact);
        equipments.add(Dungeon.hero.belongings.misc);
        equipments.add(Dungeon.hero.belongings.ring);

        return equipments;
    }

    public static JSONObject getEquipmentsJSON() throws JSONException {
        JSONObject result = new JSONObject();

        result.put("weapon", Dungeon.hero.belongings.weapon);
        result.put("armor", Dungeon.hero.belongings.armor);
        result.put("artifact", Dungeon.hero.belongings.artifact);
        result.put("misc", Dungeon.hero.belongings.misc);
        result.put("ring", Dungeon.hero.belongings.ring);

        return result;
    }

    public static ArrayList<Item> getInventoryItems() {
        return Dungeon.hero.belongings.getAllItems(Item.class);
    }

    /**
     * Get the item that is currently equipped in the same slot as the given item.
     * @param item The item to equip.
     * @return The item that is currently equipped in the same slot as the given item.
     */
    public static EquipableItem getOrigEquippedItem(EquipableItem item) {
        EquipableItem origEquipment = null;

        if(item.getClass().isInstance(Dungeon.hero.belongings.weapon)){
            origEquipment = Dungeon.hero.belongings.weapon;

        } else if(item.getClass().isInstance(Dungeon.hero.belongings.armor)){
            origEquipment = Dungeon.hero.belongings.armor;

        } else if(item.getClass().isInstance(Dungeon.hero.belongings.artifact)){
            origEquipment = Dungeon.hero.belongings.artifact;

        } else if(item.getClass().isInstance(Dungeon.hero.belongings.ring)){
            origEquipment = Dungeon.hero.belongings.ring;

        } else if(item.getClass().isInstance(Dungeon.hero.belongings.misc)){
            origEquipment = Dungeon.hero.belongings.misc;
        }

        return origEquipment;
    }

    public static JSONObject getInventoryItemsJSON() {
        JSONObject result = new JSONObject();
        result.put("items", getInventoryItems());
        return result;
    }

    public static ArrayList<Notes.KeyRecord> getKeys() {
        return Notes.getRecords(Notes.KeyRecord.class);
    }

    public static JSONObject getKeysJSON() {
        JSONObject result = new JSONObject();

        for (Notes.KeyRecord kr : getKeys()) {
            JSONObject key = new JSONObject();

            key.put("quantity", kr.quantity());
            key.put("depth", kr.depth());

            result.put(kr.desc().replace(" ", "_").toLowerCase(), key);
        }

        return result;
    }

    public static JSONObject getAllItems() throws JSONException {
        JSONObject result = getEquipmentsJSON();
        result.put("items", getInventoryItems());
        return result;
    }
}

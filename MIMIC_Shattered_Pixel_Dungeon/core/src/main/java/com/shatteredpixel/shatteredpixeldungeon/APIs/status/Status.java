package com.shatteredpixel.shatteredpixeldungeon.APIs.status;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.remains.RemainsItem;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Spell;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class Status {

    public static int getHeroHealth() {
        return Dungeon.hero.HP;
    }

    public static int getHeroMaxHealth() {
        return Dungeon.hero.HT;
    }

    public static int getHeroLevel() {
        return Dungeon.hero.lvl;
    }

    public static int getHeroExperience() {
        return Dungeon.hero.exp;
    }

    public static int getHeroMaxExperience() {
        return Dungeon.hero.maxExp();
    }

    public static int getHeroGold() {
        return Dungeon.gold;
    }

    public static ArrayList<ArrayList<Talent>> getHeroTalents() {
        ArrayList<LinkedHashMap<Talent, Integer>> talents = Dungeon.hero.talents;
        ArrayList<ArrayList<Talent>> talentList = new ArrayList<>();

        for (LinkedHashMap<Talent, Integer> tier : talents) {
            if (tier.isEmpty())
                continue;

            ArrayList<Talent> tierList = new ArrayList<>(tier.keySet());
            talentList.add(tierList);
        }

        return talentList;
    }

    public static ArrayList<ArrayList<Talent>> getCurrHeroTalents() {
        ArrayList<LinkedHashMap<Talent, Integer>> talents = Dungeon.hero.talents;
        ArrayList<ArrayList<Talent>> talentList = new ArrayList<>();

        int tierNum = getHeroTier();
        for (int i = 0; i < tierNum; i++) {
            LinkedHashMap<Talent, Integer> tier = talents.get(i);
            if (tier.isEmpty())
                continue;

            ArrayList<Talent> tierList = new ArrayList<>(tier.keySet());
            talentList.add(tierList);
        }

        return talentList;
    }

    /**
     * Get all the talents of the hero in the form of a JSON object
     * @return The talents of the hero
     */
    public static ArrayList<JSONObject> getHeroTalentsJSON() {
        ArrayList<LinkedHashMap<Talent, Integer>> talents = Dungeon.hero.talents;
        ArrayList<JSONObject> talentList = new ArrayList<>();
        int tierNumber = 1;

        // Each tier is a LinkedHashMap
        for (LinkedHashMap<Talent, Integer> tier : talents) {
            JSONObject talentJSON = new JSONObject();
            if (tier.isEmpty())
                continue;

            // Each talent in each tier
            for (Talent talent : tier.keySet()) {
                JSONObject talentFeatures = new JSONObject();

                try {
                    talentFeatures.put("tier", tierNumber);
                    talentFeatures.put("level", tier.get(talent));
                    talentFeatures.put("description", talent.desc().replace("_", "").replace("\n", ""));

                    talentJSON.put(talent.toString().replace(" ", "_").toLowerCase(), talentFeatures);

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            talentList.add(talentJSON);
            tierNumber++;
        }

        return talentList;
    }

    public static ArrayList<JSONObject> getHeroCurrTalentJSON() {
        ArrayList<JSONObject> talents = getHeroTalentsJSON();
        ArrayList<JSONObject> currTalents = new ArrayList<>();

        for (int i = 0; i < getHeroTier(); i++) {
            currTalents.add(talents.get(i));
        }

        return currTalents;
    }

    /**
     * Get the free talent points of the hero in the form of a JSON object for each tier
     * @return The free talent points of the hero
     */
    public static JSONObject getHeroFreeTalentPoints() {
        JSONObject freeTalents = new JSONObject();

        for (int i = 1; i <= Dungeon.hero.talents.size(); i++){
            freeTalents.put("tier" + i, Dungeon.hero.talentPointsAvailable(i));
        }

        return freeTalents;
    }

    public static int getHeroTier() {
        ArrayList<JSONObject> talents = getHeroTalentsJSON();
        int tiersAvailable = 1;

        while (tiersAvailable < Talent.MAX_TALENT_TIERS
                && Dungeon.hero.lvl + 1 >= Talent.tierLevelThresholds[tiersAvailable + 1]){
            tiersAvailable++;
        }

        if (tiersAvailable > 2 && Dungeon.hero.subClass == HeroSubClass.NONE){
            tiersAvailable = 2;

        } else if (tiersAvailable > 3 && Dungeon.hero.armorAbility == null){
            tiersAvailable = 3;
        }

        return Math.min(tiersAvailable, talents.size());
    }

    public static int getHeroStrength() {
        return Dungeon.hero.STR;
    }

    /**
     * Get the buffs of the hero in the form of a JSON object; Hunger is a special case and is handled separately
     * @return The buffs of the hero
     */
    public static JSONObject getHeroBuffs() {
        LinkedHashSet<Buff> buffs = Dungeon.hero.buffs();
        JSONObject buffList = new JSONObject();

        for (Buff buff : buffs) {
            if (buff.name().equals(Messages.NO_TEXT_FOUND)) continue;

            JSONObject buffJSON = new JSONObject();

            try {
                // TODO: Show the Hunger description to the agent
                if (buff instanceof Hunger) {
                    // If it is not hunger or starving, do not show it
                    if (!((Hunger) buff).isHunger() && !((Hunger) buff).isStarving()) {
                        continue;
                    }

                    buffJSON.put("type", buff.type);
                    buffJSON.put("description", buff.desc().replace("_", "").replace("\n", ""));
                    buffList.put(buff.name().replace(" ", "_").toLowerCase(), buffJSON);
                    continue;
                }

                buffJSON.put("duration", buff.visualcooldown());
                buffJSON.put("type", buff.type);
                buffJSON.put("description", buff.desc().replace("_", "").replace("\n", ""));
                buffList.put(buff.name().replace(" ", "_").toLowerCase(), buffJSON);

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        return buffList;
    }

    /**
     * Get the equipments of the hero in the form of a JSON object
     * @return The equipments of the hero
     */
    public static JSONObject getEquipments() {
        JSONObject equipments = Inventory.getEquipmentsJSON();
        JSONObject equipmentList = new JSONObject();

        for ( Iterator<String> it = equipments.keys(); it.hasNext(); ) {
            JSONObject itemFeatures = new JSONObject();
            String category = it.next();
            EquipableItem eq = (EquipableItem) equipments.get(category);

            itemFeatures.put("category", category);
            itemFeatures.put("quantity", eq.quantity());
            itemFeatures.put("level", eq.level());
            itemFeatures.put("identified", eq.isIdentified());
            itemFeatures.put("description", eq.info().replace("_", "").replace("\n", ""));

            if (eq instanceof Weapon) {
                Weapon weapon = (Weapon) eq;

                itemFeatures.put("STRReq", weapon.STRReq());

                if (weapon.enchantment != null)
                    itemFeatures.put("enchantment", weapon.enchantment.name() +
                            " - " +
                            weapon.enchantment.desc().replace("_", "").replace("\n", ""));
            }

            // Show the armor glyph if the armor is identified
            else if (eq instanceof Armor) {
                Armor armor = (Armor) eq;

                itemFeatures.put("STRReq", armor.STRReq());

                if (armor.glyph != null)
                    itemFeatures.put("glyph", armor.glyph.name() +
                            " - " +
                            armor.glyph.desc().replace("_", "").replace("\n", ""));
            }

            equipmentList.put(eq.name().replace(" ", "_").toLowerCase(), itemFeatures);
        }

        return equipmentList;
    }

    /**
     * Get the items in the hero's inventory in the form of a JSON object
     * @return The items in the hero's inventory
     */
    public static JSONObject getItems() {
        JSONObject items = new JSONObject();

        for (Item item : Dungeon.hero.belongings.getAllItems(Item.class)) {

            // Skip the equipped items as they are already shown in the equipments
            if (item instanceof EquipableItem && item.isEquipped(Dungeon.hero))
                continue;

            JSONObject itemFeatures = new JSONObject();

            try {
                itemFeatures.put("category", getCategoryName(item));
                itemFeatures.put("quantity", item.quantity());
                itemFeatures.put("level", item.level());
                itemFeatures.put("identified", item.isIdentified());

                // Show the weapon enchantment if the weapon is identified
                if (item instanceof Weapon) {
                    Weapon weapon = (Weapon) item;

                    itemFeatures.put("STRReq", weapon.STRReq());

                    if (weapon.enchantment != null)
                        itemFeatures.put("enchantment", weapon.enchantment.name() +
                                " - " +
                                weapon.enchantment.desc().replace("_", "").replace("\n", ""));
                }

                // Show the armor glyph if the armor is identified
                else if (item instanceof Armor) {
                    Armor armor = (Armor) item;

                    itemFeatures.put("STRReq", armor.STRReq());

                    if (armor.glyph != null)
                        itemFeatures.put("glyph", armor.glyph.name() +
                                " - " +
                                armor.glyph.desc().replace("_", "").replace("\n", ""));
                }

                // Any items that is not identified and not an equipable item do not have descriptions
                if (!item.isIdentified() && !(item instanceof EquipableItem))
                    itemFeatures.put("description", "unknown");

                else
                    itemFeatures.put("description", item.info().replace("_", "").replace("\n", ""));

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            items.put(item.name().replace(" ", "_").toLowerCase(), itemFeatures);
        }
        return items;
    }

    /**
     * Get the category name of an item according to its class
     * @param item The item to get the category name
     * @return The category name of the item
     */
    public static String getCategoryName(Item item) {
        if (item instanceof Armor) {
            return "armor";

        } else if (item instanceof Artifact) {
            return "artifact";

        } else if (item instanceof Bag) {
            return "bag";

        } else if (item instanceof Bomb) {
            return "bomb";

        } else if (item instanceof Food) {
            return "food";

        } else if (item instanceof Potion) {
            return "potion";

        } else if (item instanceof RemainsItem) {
            return "remains item";

        } else if (item instanceof Ring) {
            return "ring";

        } else if (item instanceof Scroll) {
            return "scroll";

        } else if (item instanceof Spell) {
            return "spell";

        } else if (item instanceof Wand) {
            return "wand";

        } else if (item instanceof Weapon) {
            return "weapon";

        } else if (item instanceof Plant.Seed) {
            return "seed";

        } else {
            return item.name();
        }
    }

    /**
     * Get the status of the hero in the form of a JSON object
     * @return The status of the hero
     * @throws JSONException The exception thrown when there is an error in the JSON object
     */
    public static JSONObject getStatus() throws JSONException {
        JSONObject result = new JSONObject();

        // Equipments
        result.put("equipments", getEquipments());

        // Inventory
        result.put("items", getItems());
        result.put("keys", Inventory.getKeysJSON());

        // Position
        result.put("heroPositionInXY", Position.getHeroPositionXY());

        // Hero Status
        result.put("health", getHeroHealth());
        result.put("maxHealth", getHeroMaxHealth());
        result.put("level", getHeroLevel());
        result.put("experience", getHeroExperience());
        result.put("maxExperience", getHeroMaxExperience());
        result.put("gold", getHeroGold());
        result.put("freeTalentPoints", getHeroFreeTalentPoints());
        result.put("talents", getHeroTalentsJSON());
        result.put("currTalents", getHeroCurrTalentJSON());
        result.put("tier", getHeroTier());
        result.put("strength", getHeroStrength());
        result.put("buffs", getHeroBuffs());
        result.put("depth", Dungeon.depth);
        result.put("mapSeed", Dungeon.seed);

        // Environment
        result.put("environment", Environment.getImportantTilesXYJSON());

        return result;
    }
}

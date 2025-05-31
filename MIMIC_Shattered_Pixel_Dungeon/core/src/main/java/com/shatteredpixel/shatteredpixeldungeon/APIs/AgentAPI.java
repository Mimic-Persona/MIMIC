package com.shatteredpixel.shatteredpixeldungeon.APIs;

import com.shatteredpixel.shatteredpixeldungeon.APIs.status.Inventory;
import com.shatteredpixel.shatteredpixeldungeon.APIs.status.Position;
import com.shatteredpixel.shatteredpixeldungeon.APIs.status.Status;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.HoldFast;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Waterskin;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.remains.RemainsItem;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.InventoryScroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.CursedWand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Toolbar;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;
import com.watabou.utils.Callback;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import static com.shatteredpixel.shatteredpixeldungeon.items.Item.AC_THROW;

public class AgentAPI extends PixelScene {

    private static final int COUNTER_INIT = 500;
    private static int counter = COUNTER_INIT;
    private static String command = "";
    private static boolean isSelected = false;
    public static boolean isWndShown = false;

    public static void handle() throws JSONException {
        if (Dungeon.isAgentNext) {
            Dungeon.isAgentNext = false;
            if (!isWndShown) {
                isWndShown = true;
                ShatteredPixelDungeon.scene().addToFront(new WndTextInput("Enter your command: ",
                        "",
                        command,
                        10,
                        false,
                        Messages.get(Char.class, "agent_set"),
                        Messages.get(Char.class, "agent_clear")) {
                    @Override
                    public void onSelect(boolean positive, String text) {
                        isSelected = true;
                        command = text;
                    }
                });
            }
        }

        if (command.equals("m") || command.equals("s") || command.equals("1") || command.equals("l") || command.equals("k")) {
            JSONObject commandJSON = new JSONObject();
            commandJSON.put("msgType", "command");
            commandJSON.put("command", command);
            Dungeon.gameServer.broadcast(commandJSON.toString());

            if (!command.equals("1") && !command.equals("l") && !command.equals("k")) isWndShown = false;
            Dungeon.isAgentNext = true;
            command = "";
            counter = COUNTER_INIT;
            isSelected = false;

        } else {
            if (isSelected) {
                GLog.e("Invalid Command " + command);
                Dungeon.isAgentNext = true;
                command = "";
                counter = COUNTER_INIT;
                isSelected = false;
            }
        }
    }

    public static boolean handle(String action, int[] tile, String item1, String item2, int waitTurns) throws JSONException {
        boolean res = false;
        counter = COUNTER_INIT;

        if (action.equals("act")) {
            res = act(tile);

        } else if (action.equals("wait")) {
            res = heroWait(waitTurns);
            GameScene.isUpdated = true;

        } else if (action.equals("equip")) {
            res = equip(item1);
            GameScene.isUpdated = true;

        } else if (action.equals("unequip")) {
            res = unEquip(item1);
            GameScene.isUpdated = true;

        } else if (action.equals("drop")) {
            res = drop(item1);
            GameScene.isUpdated = true;

        } else if (action.equals("use")) {
            res = use(tile, item1, item2);
            GameScene.isUpdated = true;

        } else if (action.equals("throw")) {
            res = throwItem(tile, item1);
            GameScene.isUpdated = true;

        } else if (action.equals("upgrade")) {
            res = upgrade(item1);
            GameScene.isUpdated = true;

        } else {
            GLog.e("Invalid Action " + action);
        }

        while (counter > 0) {
            counter--;
        }

        Dungeon.isAgentNext = true;
        return res;
    }

    // ==================================================================================
    // ==================================== Actions =====================================
    // ==================================================================================

    public static boolean act(int[] tile) {
        int X = tile[0];
        int Y = tile[1];

        int cell = Position.transferXY2Pos(X, Y);

        if (Dungeon.hero.handle(cell)) {
            Dungeon.hero.next();
            return true;
        }

        return false;
    }

    /**
     * Let the hero wait for 1 turn
     * @return true if the hero is ready to wait; false otherwise
     */
    public static boolean heroWait() {
        GLog.w("Wait");

        if (Dungeon.hero != null && Dungeon.hero.ready && !GameScene.cancel()) {
            Toolbar.examining = false;
            Dungeon.hero.rest(false);
            return true;
        }

        return false;
    }

    /**
     * Let the hero wait for a certain number of turns
     * @param waitTurns the number of turns to wait
     * @return true if the hero waits; false otherwise
     */
    private static boolean heroWait(int waitTurns) {
        Dungeon.waitCounter = 0;

        if (Dungeon.hero.isStarving()) {
            GLog.e("You cannot wait while you are starving, this will deal damage to you.");

        } else if (Dungeon.hero != null && Dungeon.hero.ready && !GameScene.cancel()) {
            GLog.w("Wait for " + waitTurns + " turns.");
            Toolbar.examining = false;
            Dungeon.hero.rest(true);
            Dungeon.waitTurns = waitTurns;
            return true;

        } else {
            GLog.e("Hero is not ready to wait.");
        }

        return false;
    }

    public static boolean equip(String item1) {
        EquipableItem toBeEquipped = null;

        for (EquipableItem i : Dungeon.hero.belongings.getAllItems(EquipableItem.class)){
            if (toBeEquipped == null && i.toString().equals(item1.replaceAll(" ", "_").toLowerCase())) {
                toBeEquipped = i;
            }
        }

        if (toBeEquipped == null) {
            GLog.e(String.format("Equipment \"" + item1 + "\" is not in the inventory."));
            return false;
        }

        EquipableItem origEquipment = Inventory.getOrigEquippedItem(toBeEquipped);

        if (origEquipment != null) GLog.w("Equip: " + item1 + " to change: " + origEquipment.name());
        else GLog.w("Equip: " + item1);

        toBeEquipped.execute(Dungeon.hero, EquipableItem.AC_EQUIP);

        return true;
    }

    public static boolean unEquip(String item1) {
        EquipableItem toBeUnEquipped = null;

        for (EquipableItem i : Dungeon.hero.belongings.getEquipments()){
            if (toBeUnEquipped == null && i.toString().equals(item1.replaceAll(" ", "_").toLowerCase())) {
                toBeUnEquipped = i;
            }
        }

        if (toBeUnEquipped == null) {
            GLog.e(String.format("Equipment \"" + item1 + "\" is not equipped."));
            return false;
        }

        GLog.w("Unequip: " + item1);

        toBeUnEquipped.execute(Dungeon.hero, EquipableItem.AC_UNEQUIP);

        return true;
    }

    public static boolean drop(String item1) {
        Item toBeDropped = null;

        for (Item i : Dungeon.hero.belongings.getAllItems(Item.class)){
            if (toBeDropped == null && i.toString().equals(item1.replaceAll(" ", "_").toLowerCase())) {
                toBeDropped = i;
            }
        }

        if (toBeDropped == null) {
            GLog.e(String.format("Item \"" + item1 + "\" is not in the inventory."));
            return false;
        }

        GLog.w("Drop: " + item1);

        toBeDropped.execute(Dungeon.hero, Item.AC_DROP);

        return true;
    }

    // TODO: Special case: scroll of metamorphosis, choose a talent to change
    public static boolean useOnto(InventoryScroll scroll, String item) {
        Item subjectItem = null;

        for (Item i : Dungeon.hero.belongings.getAllItems(Item.class)) {
            if (subjectItem == null && i.toString().equals(item.replaceAll(" ", "_").toLowerCase())) {
                subjectItem = i;
            }
        }

        for (Item i : Dungeon.hero.belongings.getEquipments()){
            if (subjectItem == null && i.toString().equals(item.replaceAll(" ", "_").toLowerCase())) {
                subjectItem = i;
            }
        }

        if (subjectItem == null) {
            GLog.e(String.format("Item \"" + item + "\" is not in the inventory."));
            return false;
        }

        scroll.execute(Dungeon.hero, Scroll.AC_READ);

        if (!scroll.doIdentify(subjectItem)) {
            GLog.e(String.format("Item \"" + item + "\" cannot be used as the subject item."));
            return false;
        }

        GLog.w("Read Scroll: " + item + " to: " + item);

        return true;
    }

    public static boolean use(int[] tile, String item1, String item2) {
        Item toBeUsed = null;

        for (Item i : Dungeon.hero.belongings.getAllItems(Item.class)){
            if (toBeUsed == null && i.toString().equals(item1.replaceAll(" ", "_").toLowerCase())) {
                toBeUsed = i;
            }
        }

        for (Item i : Dungeon.hero.belongings.getEquipments()){
            if (toBeUsed == null && i.toString().equals(item1.replaceAll(" ", "_").toLowerCase())) {
                toBeUsed = i;
            }
        }

        if (toBeUsed == null) {
            GLog.e(String.format("Item \"" + item1 + "\" is not in the inventory."));
            return false;
        }

        if (toBeUsed instanceof Food) {
            GLog.w("Eat Food: " + item1);
            toBeUsed.execute(Dungeon.hero, Food.AC_EAT);

        } else if (toBeUsed instanceof Potion) {
            GLog.w("Drink Potion: " + item1);
            toBeUsed.execute(Dungeon.hero, Potion.AC_DRINK);

        } else if (toBeUsed instanceof RemainsItem) {
            GLog.w("Use RemainsItem: " + item1);
            toBeUsed.execute(Dungeon.hero, RemainsItem.AC_USE);

        } else if (toBeUsed instanceof Scroll) {
            if (toBeUsed instanceof InventoryScroll) {
                InventoryScroll scroll = (InventoryScroll) toBeUsed;
                return useOnto(scroll, item2);

            } else {
                GLog.w("Read Scroll: " + item1);
                toBeUsed.execute(Dungeon.hero, Scroll.AC_READ);
            }

        } else if (toBeUsed instanceof Wand) {
            GLog.w("Zap Wand: " + item1);

            if (tile == null || tile[0] < 0 || tile[1] < 0 || tile[0] >= Dungeon.level.width() || tile[1] >= Dungeon.level.height()) {
                GLog.e("Unknown target position.");
                return false;
            }
            int target = Position.transferXY2Pos(tile[0], tile[1]);
            Wand wand = (Wand) toBeUsed;

            GLog.w("Zap: " + wand + " to: " + Arrays.toString(Position.transferPos2XY(target)));

            wand.execute(Dungeon.hero, Wand.ACG_ZAP);

            final Ballistica shot = new Ballistica( Dungeon.hero.pos, target, wand.collisionProperties(target));
            int cell = shot.collisionPos;

            if (target == Dungeon.hero.pos || cell == Dungeon.hero.pos) {
                GLog.i( Messages.get(Wand.class, "self_target") );
                return false;
            }

            Dungeon.hero.sprite.zap(cell);

            //attempts to target the cell aimed at if something is there, otherwise targets the collision pos.
            if (Actor.findChar(target) != null)
                QuickSlotButton.target(Actor.findChar(target));
            else
                QuickSlotButton.target(Actor.findChar(cell));

            if (wand.tryToZap(Dungeon.hero, target)) {

                Dungeon.hero.busy();

                if (wand.cursed) {
                    if (!wand.cursedKnown) {
                        GLog.n(Messages.get(Wand.class, "curse_discover", wand.name()));
                    }
                    CursedWand.cursedZap(wand,
                            Dungeon.hero,
                            new Ballistica(Dungeon.hero.pos, target, Ballistica.MAGIC_BOLT),
                            new Callback() {
                                @Override
                                public void call() {
                                    wand.wandUsed();
                                }
                            });
                } else {
                    wand.fx(shot, new Callback() {
                        public void call() {
                            wand.onZap(shot);
                            wand.wandUsed();
                        }
                    });
                }
                wand.cursedKnown = true;
            }

        } else if (toBeUsed instanceof Waterskin) {
            GLog.w("Drink Waterskin");
            toBeUsed.execute(Dungeon.hero, Waterskin.AC_DRINK);

        } else {
            GLog.e(String.format("Item \"" + item1 + "\" cannot be used."));
            return false;
        }

        return true;
    }

    public static boolean throwItem(int[] tile, String item) {
        if (tile == null || tile[0] < 0 || tile[1] < 0 || tile[0] >= Dungeon.level.width() || tile[1] >= Dungeon.level.height()) {
            GLog.e("Unknown target position.");
            return false;
        }

        int X = tile[0];
        int Y = tile[1];

        int cell = Position.transferXY2Pos(X, Y);

        return throwItem(cell, item);
    }

    public static boolean throwItem(int cell, String item) {
        Item toBeThrown = null;

        for (Item i : Dungeon.hero.belongings.getAllItems(Item.class)){
            if (toBeThrown == null && i.toString().equals(item.replaceAll(" ", "_").toLowerCase())) {
                toBeThrown = i;
            }
        }

        for (Item i : Dungeon.hero.belongings.getEquipments()){
            if (toBeThrown == null && i.toString().equals(item.replaceAll(" ", "_").toLowerCase())) {
                toBeThrown = i;
            }
        }

        if (toBeThrown == null) {
            GLog.e(String.format("Item \"" + item + "\" is not in the inventory."));
            return false;
        }

        if (!toBeThrown.actions(Dungeon.hero).contains(AC_THROW)) {
            GLog.e(String.format("Item \"" + item + "\" cannot be thrown."));
            return false;
        }

        GLog.w("Throw: " + item + " to: " + Arrays.toString(Position.transferPos2XY(cell)));
        toBeThrown.cast(Dungeon.hero, cell);

        Dungeon.prevAction = new JSONObject();
        Dungeon.prevAction.put( "action", "Throw" );
        Dungeon.prevAction.put( "obj1", toBeThrown.trueName() );

//        toBeThrown.execute(Dungeon.hero, AC_THROW);
//
//        GameScene.handleCell(cell);

        return true;
    }

    public static boolean upgrade(String talent) {
        // Check exception
        if (!Dungeon.hero.isAlive()){
            GLog.e("Hero is dead.");
            return false;
        }

        // Check if point available
        boolean unspentTalents = false;
        for (int i = 1; i <= Dungeon.hero.talents.size(); i++){
            if (Dungeon.hero.talentPointsAvailable(i) > 0){
                unspentTalents = true;
                break;
            }
        }

        if (!unspentTalents) {
            GLog.e("No talent point available.");
            return false;
        }

        Talent toBeUpgraded = null;

        // Check if the talent exists
        ArrayList<ArrayList<Talent>> allTalents = Status.getHeroTalents();

        outer:
        for (ArrayList<Talent> tier : allTalents){
            for (Talent t : tier){
                if (t.name().replaceAll(" ", "_").equalsIgnoreCase(talent.replaceAll(" ", "_"))) {
                    toBeUpgraded = t;
                    break outer;
                }
            }
        }

        if (toBeUpgraded == null) {
            GLog.e(String.format("Talent \"" + talent + "\" does not exist."));
            return false;
        }

        toBeUpgraded = null;
        int tierNumber = 0;

        // Check if the talent is available for current tier
        ArrayList<ArrayList<Talent>> currTalents = Status.getCurrHeroTalents();

        outer:
        for (ArrayList<Talent> tier : currTalents){
            tierNumber ++;
            for (Talent t : tier){
                if (t.name().replaceAll(" ", "_").equalsIgnoreCase(talent.replaceAll(" ", "_"))) {
                    toBeUpgraded = t;
                    break outer;
                }
            }
        }

        if (toBeUpgraded == null) {
            GLog.e(String.format("Talent \"" + talent + "\" is not available for current hero tier."));
            return false;
        }

        // Check if the talent points in the tier is available
        if (Dungeon.hero.talentPointsAvailable(tierNumber) <= 0) {
            GLog.e(String.format("Talent \"" + talent + "\" cannot be upgraded since not enough point in tier" + tierNumber));
            return false;
        }

        // Check if the talent is already maxed
        if (Dungeon.hero.pointsInTalent(toBeUpgraded) >= toBeUpgraded.maxPoints()) {
            GLog.e(String.format("Talent \"" + talent + "\" cannot be upgraded since talent it is already maxed."));
            return false;
        }

        GLog.w("Upgrade: " + talent);
        Dungeon.hero.upgradeTalent(toBeUpgraded);

        return true;
    }

    // ==================================================================================
    // =============================== Helper Functions =================================
    // ==================================================================================
}

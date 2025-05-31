package com.shatteredpixel.shatteredpixeldungeon.APIs;

import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.APIs.status.Environment;
import com.shatteredpixel.shatteredpixeldungeon.APIs.status.Position;
import com.shatteredpixel.shatteredpixeldungeon.APIs.status.Status;
import com.shatteredpixel.shatteredpixeldungeon.APIs.status.Inventory;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.HoldFast;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.remains.RemainsItem;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.InventoryScroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.CursedWand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Toolbar;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static com.shatteredpixel.shatteredpixeldungeon.items.Item.AC_THROW;

public class ManualAPI extends PixelScene {
    private final int COUNTER_INIT = 1000;
    public String command;
    public int cell;

    private int X;
    private int Y;

    private boolean isXWndShown;
    private boolean isYWndShown;

    private int currX;
    private int currY;
    private int counter;

    private String equipment;
    private EquipableItem toBeEquipped;
    private boolean isSelected;
    private boolean isEqpWndShown;

    private int waitTurns;

    private EquipableItem toBeUnEquipped;

    private Item toBeDropped;

    private Item toBeUsed;
    private String item;
    private boolean isIdentifyWndShown;
    private boolean isISelected;

    private String identifiedItem;
    private Item toBeIdentified;

    private Talent toBeUpgraded;
    private String talent;

    public ManualAPI() {
        command = "";
        X = -1;
        Y = -1;
        isXWndShown = false;
        isYWndShown = false;
        currX = Position.getHeroPositionXY()[0];
        currY = Position.getHeroPositionXY()[1];
        counter = COUNTER_INIT;

        equipment = "";
        isSelected = false;
        toBeEquipped = null;
        isEqpWndShown = false;

        waitTurns = 0;

        toBeUnEquipped = null;

        toBeDropped = null;

        item = "";
        identifiedItem = "";
        toBeUsed = null;
        isIdentifyWndShown = false;
        isISelected = false;

        toBeUpgraded = null;
        talent = "";
    }

    public void handle() throws JSONException {
        while (counter > 0) {
            counter--;
        }

        currX = Position.getHeroPositionXY()[0];
        currY = Position.getHeroPositionXY()[1];

        if (Dungeon.isAgentNext && !Dungeon.hero.resting) {
            Dungeon.isAgentNext = false;
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

        if (command.equals("a")) {
            act();
        } else if (command.equals("w")) {
            heroWait();
        } else if (command.equals("e")) {
            equip();
        } else if (command.equals("ue")) {
            unEquip();
        } else if (command.equals("d")) {
            drop();
        } else if (command.equals("u")) {
            use();
        } else if (command.equals("t")) {
            throwItem();
        } else if (command.equals("up")) {
            upgrade();
        } else if (command.equals("1")) {
            GLog.w(Status.getStatus().toString());
            if (isSelected) {
                Dungeon.isAgentNext = true;
                command = "";
                counter = COUNTER_INIT;
                isSelected = false;
                isEqpWndShown = false;
            }

        } else if (command.equals("2")) {
            GLog.w(Arrays.deepToString(Environment.getTilesXY()));
            if (isSelected) {
                Dungeon.isAgentNext = true;
                command = "";
                counter = COUNTER_INIT;
                isSelected = false;
                isEqpWndShown = false;
            }

        } else if (command.equals("3")) {
            GLog.w(Arrays.deepToString(Environment.getVisibleTilesXY()));
            if (isSelected) {
                Dungeon.isAgentNext = true;
                command = "";
                counter = COUNTER_INIT;
                isSelected = false;
                isEqpWndShown = false;
            }

        } else if (command.equals("4")) {
            GLog.w(Environment.getImportantTilesXYJSON().toString());
            if (isSelected) {
                Dungeon.isAgentNext = true;
                command = "";
                counter = COUNTER_INIT;
                isSelected = false;
                isEqpWndShown = false;
            }

        } else {
            if (isSelected) {
                GLog.e("Invalid Command " + command);
                Dungeon.isAgentNext = true;
                command = "";
                counter = COUNTER_INIT;
                equipment = "";
                isSelected = false;
                isEqpWndShown = false;
            }
        }
    }

    // ==================================================================================
    // ==================================== Actions =====================================
    // ==================================================================================

    private void act() {
        if (!isXWndShown) {
            isXWndShown = true;
            ShatteredPixelDungeon.scene().addToFront(new WndTextInput("Your Current X is" + currX + ". Enter your X: ",
                    "",
                    String.valueOf(currX),
                    4,
                    false,
                    Messages.get(Char.class, "agent_set"),
                    Messages.get(Char.class, "agent_clear")) {
                @Override
                public void onSelect(boolean positive, String text) {
                    if (Objects.equals(text, "") || Integer.parseInt(text) < 0) {
                        GLog.e("Invalid X.");
                        isXWndShown = false;
                        return;
                    }
                    X = Integer.parseInt(text);
                }
            });
        }

        if (X >= 0 && !isYWndShown) {
            isYWndShown = true;
            ShatteredPixelDungeon.scene().addToFront(new WndTextInput("Your Current Y is" + currY + ". Enter your Y: ",
                    "",
                    String.valueOf(currY),
                    4,
                    false,
                    Messages.get(Char.class, "agent_set"),
                    Messages.get(Char.class, "agent_clear")) {
                @Override
                public void onSelect(boolean positive, String text) {
                    if (Objects.equals(text, "") || Integer.parseInt(text) < 0) {
                        GLog.e("Invalid Y.");
                        isYWndShown = false;
                        return;
                    }
                    Y = Integer.parseInt(text);
                }
            });
        }

        if (X < 0 || Y < 0) {
            return;
        }

        this.cell = Position.transferXY2Pos(X, Y);

        if (Dungeon.hero.handle( cell )) {
            Dungeon.hero.next();
        }

        GameScene.isUpdated = false;

        Dungeon.isAgentNext = true;
        command = "";
        X = -1;
        Y = -1;
        isXWndShown = false;
        isYWndShown = false;
        counter = COUNTER_INIT;
        isSelected = false;
    }

    private void explore() {
        if (!isXWndShown) {
            isXWndShown = true;
            ShatteredPixelDungeon.scene().addToFront(new WndTextInput("Your Current X is" + currX + ". Enter your X: ",
                    "",
                    String.valueOf(currX),
                    4,
                    false,
                    Messages.get(Char.class, "agent_set"),
                    Messages.get(Char.class, "agent_clear")) {
                @Override
                public void onSelect(boolean positive, String text) {
                    if (Objects.equals(text, "") || Integer.parseInt(text) < 0) {
                        GLog.e("Invalid X.");
                        isXWndShown = false;
                        return;
                    }
                    X = Integer.parseInt(text);
                }
            });
        }

        if (X >= 0 && !isYWndShown) {
            isYWndShown = true;
            ShatteredPixelDungeon.scene().addToFront(new WndTextInput("Your Current Y is" + currY + ". Enter your Y: ",
                    "",
                    String.valueOf(currY),
                    4,
                    false,
                    Messages.get(Char.class, "agent_set"),
                    Messages.get(Char.class, "agent_clear")) {
                @Override
                public void onSelect(boolean positive, String text) {
                    if (Objects.equals(text, "") || Integer.parseInt(text) < 0) {
                        GLog.e("Invalid Y.");
                        isYWndShown = false;
                        return;
                    }
                    Y = Integer.parseInt(text);
                }
            });
        }

        if (X < 0 || Y < 0) {
            return;
        }

        explore(X, Y);

        GameScene.isUpdated = false;

        Dungeon.isAgentNext = true;
        command = "";
        X = -1;
        Y = -1;
        isXWndShown = false;
        isYWndShown = false;
        counter = COUNTER_INIT;
        isSelected = false;
    }

    /**
     * Explore the map in the direction of (x, y)
     * Strategies:
     * 1) Explore the cell that is at the boundary of the unexplored area in the given direction
     * 2) Prioritize the cell that is closer to the hero if that direction has nothing
     * 3) Prioritize the boundary with terrain that is 1) door, 2) not wall.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    private void explore(int x, int y) {
        int cell = Position.transferXY2Pos(x, y);

        if (Dungeon.hero.handle( cell )) {
            Dungeon.hero.next();
        }
    }

    private void heroWait() {
        if (!isEqpWndShown) {
            isEqpWndShown = true;
            isSelected = false;
            ShatteredPixelDungeon.scene().addToFront(new WndTextInput("How long do you want to wait?",
                    "",
                    String.valueOf(waitTurns),
                    50,
                    false,
                    Messages.get(Char.class, "agent_set"),
                    Messages.get(Char.class, "agent_clear")) {
                @Override
                public void onSelect(boolean positive, String text) {
                    if (Objects.equals(text, "") || Integer.parseInt(text) <= 0) {
                        GLog.e("Invalid wait turns.");
                        return;
                    }
                    isSelected = true;
                    waitTurns = Integer.parseInt(text);
                }
            });
        }

        if (!isSelected) {
            return;
        }

        Dungeon.waitCounter = 0;

        if (Dungeon.hero.isStarving()) {
            GLog.e("You cannot wait while you are starving, this will deal damage to you.");

        } else if (Dungeon.hero != null && Dungeon.hero.ready && !GameScene.cancel()) {
            GLog.w("Wait for " + waitTurns + " turns.");
            Toolbar.examining = false;
            Dungeon.hero.rest(true);
            Dungeon.waitTurns = waitTurns;

        } else {
            GLog.e("Hero is not ready to wait.");
        }

        Dungeon.isAgentNext = true;
        waitTurns = 0;
        command = "";
        counter = COUNTER_INIT;
        equipment = "";
        toBeEquipped = null;
        isSelected = false;
        isEqpWndShown = false;
    }


    private void equip(){
        if (!isEqpWndShown) {
            isEqpWndShown = true;
            isSelected = false;
            ShatteredPixelDungeon.scene().addToFront(new WndTextInput("What do you want to equip?",
                    "",
                    equipment,
                    50,
                    false,
                    Messages.get(Char.class, "agent_set"),
                    Messages.get(Char.class, "agent_clear")) {
                @Override
                public void onSelect(boolean positive, String text) {
                    if (Objects.equals(text, "")) {
                        GLog.e("Invalid equipment.");
                        return;
                    }
                    isSelected = true;
                    equipment = text;
                }
            });
        }

        if (!isSelected) {
            return;
        }

        for (EquipableItem i : Dungeon.hero.belongings.getAllItems(EquipableItem.class)){
            if (toBeEquipped == null && i.toString().equals(equipment.replace(" ", "_").toLowerCase())) {
                toBeEquipped = i;
            }
        }

        if (isSelected && toBeEquipped == null) {
            isSelected = false;
            isEqpWndShown = false;
            GLog.e("Equipment not in the inventory.");
            return;
        }

        else if (toBeEquipped == null) {
            return;
        }

        EquipableItem origEquipment = Inventory.getOrigEquippedItem(toBeEquipped);

        if (origEquipment != null) GLog.w("Equip: " + equipment + " to change: " + origEquipment.name());
        else GLog.w("Equip: " + equipment);

        toBeEquipped.execute(Dungeon.hero, EquipableItem.AC_EQUIP);

        Dungeon.isAgentNext = true;
        command = "";
        counter = COUNTER_INIT;
        equipment = "";
        toBeEquipped = null;
        isSelected = false;
        isEqpWndShown = false;
    }

    private void unEquip(){
        if (!isEqpWndShown) {
            isEqpWndShown = true;
            isSelected = false;
            ShatteredPixelDungeon.scene().addToFront(new WndTextInput("What do you want to unequip?",
                    "",
                    equipment,
                    50,
                    false,
                    Messages.get(Char.class, "agent_set"),
                    Messages.get(Char.class, "agent_clear")) {
                @Override
                public void onSelect(boolean positive, String text) {
                    if (Objects.equals(text, "")) {
                        GLog.e("Invalid equipment.");
                        return;
                    }
                    isSelected = true;
                    equipment = text;
                }
            });
        }

        if (!isSelected) {
            return;
        }

        for (EquipableItem i : Dungeon.hero.belongings.getEquipments()){
            if (toBeUnEquipped == null && i.toString().equals(equipment.replace(" ", "_").toLowerCase())) {
                toBeUnEquipped = i;
            }
        }

        if (isSelected && toBeUnEquipped == null) {
            isSelected = false;
            isEqpWndShown = false;
            GLog.e("Equipment is not equipped.");
            return;
        }

        else if (toBeUnEquipped == null) {
            return;
        }

        GLog.w("Unequip: " + equipment);

        toBeUnEquipped.execute(Dungeon.hero, EquipableItem.AC_UNEQUIP);

        Dungeon.isAgentNext = true;
        command = "";
        counter = COUNTER_INIT;
        equipment = "";
        toBeUnEquipped = null;
        isSelected = false;
        isEqpWndShown = false;
    }

    private void drop(){
        if (!isEqpWndShown) {
            isEqpWndShown = true;
            isSelected = false;
            ShatteredPixelDungeon.scene().addToFront(new WndTextInput("What do you want to drop?",
                    "",
                    equipment,
                    50,
                    false,
                    Messages.get(Char.class, "agent_set"),
                    Messages.get(Char.class, "agent_clear")) {
                @Override
                public void onSelect(boolean positive, String text) {
                    if (Objects.equals(text, "")) {
                        GLog.e("Invalid equipment.");
                        return;
                    }
                    isSelected = true;
                    equipment = text;
                }
            });
        }

        if (!isSelected) {
            return;
        }

        for (Item i : Dungeon.hero.belongings.getAllItems(Item.class)){
            if (toBeDropped == null && i.toString().equals(equipment.replace(" ", "_").toLowerCase())) {
                toBeDropped = i;
            }
        }

        if (isSelected && toBeDropped == null) {
            isSelected = false;
            isEqpWndShown = false;
            GLog.e("Item is not in the inventory.");
            return;
        }

        else if (toBeDropped == null) {
            return;
        }

        GLog.w("Drop: " + equipment);

        toBeDropped.execute(Dungeon.hero, Item.AC_DROP);

        Dungeon.isAgentNext = true;
        command = "";
        counter = COUNTER_INIT;
        equipment = "";
        toBeDropped = null;
        isSelected = false;
        isEqpWndShown = false;
    }

    // TODO: The window stuck => item throw not workable after identifying
    // TODO: Special case: scroll of metamorphosis, choose a talent to change
    private boolean useOnto(InventoryScroll scroll) {
        if (!isIdentifyWndShown) {
            isIdentifyWndShown = true;
            isISelected = false;
            ShatteredPixelDungeon.scene().addToFront(new WndTextInput("What do you want to use onto?",
                    "",
                    identifiedItem,
                    50,
                    false,
                    Messages.get(Char.class, "agent_set"),
                    Messages.get(Char.class, "agent_clear")) {
                @Override
                public void onSelect(boolean positive, String text) {
                    if (Objects.equals(text, "")) {
                        GLog.e("Invalid item.");
                        return;
                    }
                    isISelected = true;
                    identifiedItem = text;
                }
            });
        }

        if (!isISelected) {
            return false;
        }

        for (Item i : Dungeon.hero.belongings.getAllItems(Item.class)){
            if (toBeIdentified == null && i.toString().equals(identifiedItem.replace(" ", "_").toLowerCase())) {
                toBeIdentified = i;
            }
        }

        for (Item i : Dungeon.hero.belongings.getEquipments()){
            if (toBeIdentified == null && i.toString().equals(identifiedItem.replace(" ", "_").toLowerCase())) {
                toBeIdentified = i;
            }
        }

        if (isISelected && toBeIdentified == null) {
            isISelected = false;
            isIdentifyWndShown = false;
            GLog.e("Item is not in the inventory.");
            return false;
        }

        else if (toBeIdentified == null) {
            return false;
        }

        scroll.execute(Dungeon.hero, Scroll.AC_READ);

        // redo all asking
        if (!scroll.doIdentify(toBeIdentified)) {
            GLog.e("This item cannot be identified.");
        }

        GLog.w("Read Scroll: " + item + " to: " + identifiedItem);

        identifiedItem = "";
        toBeIdentified = null;
        isISelected = false;
        isIdentifyWndShown = false;

        return true;
    }

    private boolean useOnto(Wand wand) {
        if (!isXWndShown) {
            isXWndShown = true;
            ShatteredPixelDungeon.scene().addToFront(new WndTextInput("Your Current X is" + currX + ". Enter your X: ",
                    "",
                    String.valueOf(currX),
                    4,
                    false,
                    Messages.get(Char.class, "agent_set"),
                    Messages.get(Char.class, "agent_clear")) {
                @Override
                public void onSelect(boolean positive, String text) {
                    if (Objects.equals(text, "") || Integer.parseInt(text) < 0) {
                        GLog.e("Invalid X.");
                        isXWndShown = false;
                        return;
                    }
                    X = Integer.parseInt(text);
                }
            });
        }

        if (X >= 0 && !isYWndShown) {
            isYWndShown = true;
            ShatteredPixelDungeon.scene().addToFront(new WndTextInput("Your Current Y is" + currY + ". Enter your Y: ",
                    "",
                    String.valueOf(currY),
                    4,
                    false,
                    Messages.get(Char.class, "agent_set"),
                    Messages.get(Char.class, "agent_clear")) {
                @Override
                public void onSelect(boolean positive, String text) {
                    if (Objects.equals(text, "") || Integer.parseInt(text) < 0) {
                        GLog.e("Invalid Y.");
                        isYWndShown = false;
                        return;
                    }
                    Y = Integer.parseInt(text);
                }
            });
        }

        if (X < 0 || Y < 0) {
            return false;
        }

        int target = Position.transferXY2Pos(X, Y);

        GLog.w("Zap: " + item + " to: " + Arrays.toString(Position.transferPos2XY(target)));

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

            if (wand.cursed){
                if (!wand.cursedKnown){
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

        Dungeon.isAgentNext = true;
        command = "";
        item = "";
        X = -1;
        Y = -1;
        isXWndShown = false;
        isYWndShown = false;
        counter = COUNTER_INIT;
        isSelected = false;
        toBeUsed = null;

        return true;
    }

    private void use() {
        if (!isEqpWndShown) {
            isEqpWndShown = true;
            isSelected = false;
            ShatteredPixelDungeon.scene().addToFront(new WndTextInput("What do you want to use?",
                    "",
                    item,
                    50,
                    false,
                    Messages.get(Char.class, "agent_set"),
                    Messages.get(Char.class, "agent_clear")) {
                @Override
                public void onSelect(boolean positive, String text) {
                    if (Objects.equals(text, "")) {
                        GLog.e("Invalid item.");
                        return;
                    }
                    isSelected = true;
                    item = text;
                }
            });
        }

        if (!isSelected) {
            return;
        }

        for (Item i : Dungeon.hero.belongings.getAllItems(Item.class)){
            if (toBeUsed == null && i.toString().equals(item.replace(" ", "_").toLowerCase())) {
                toBeUsed = i;
            }
        }

        for (Item i : Dungeon.hero.belongings.getEquipments()){
            if (toBeUsed == null && i.toString().equals(item.replace(" ", "_").toLowerCase())) {
                toBeUsed = i;
            }
        }

        if (isSelected && toBeUsed == null) {
            isSelected = false;
            isEqpWndShown = false;
            GLog.e("Item is not in the inventory.");
            return;
        }

        else if (toBeUsed == null) {
            return;
        }

        if (toBeUsed instanceof Food) {
            GLog.w("Eat Food: " + item);
            toBeUsed.execute(Dungeon.hero, Food.AC_EAT);

        } else if (toBeUsed instanceof Potion) {
            GLog.w("Drink Potion: " + item);
            toBeUsed.execute(Dungeon.hero, Potion.AC_DRINK);

        } else if (toBeUsed instanceof RemainsItem) {
            GLog.w("Use RemainsItem: " + item);
            toBeUsed.execute(Dungeon.hero, RemainsItem.AC_USE);

        } else if (toBeUsed instanceof Scroll) {
            if (toBeUsed instanceof InventoryScroll) {
                InventoryScroll scroll = (InventoryScroll) toBeUsed;
                if (!useOnto(scroll)) {
                    return;
                }
            } else {
                GLog.w("Read Scroll: " + item);
                toBeUsed.execute(Dungeon.hero, Scroll.AC_READ);
            }

        } else if (toBeUsed instanceof Wand) {
            Wand wand = (Wand) toBeUsed;
            if (!useOnto(wand)) {
                return;
            }

        } else if (toBeUsed instanceof Waterskin) {
            GLog.w("Drink Waterskin");
            toBeUsed.execute(Dungeon.hero, Waterskin.AC_DRINK);

        } else {
            GLog.e("Invalid item to be used.");
            isSelected = false;
            isEqpWndShown = false;
            return;
        }

        Dungeon.isAgentNext = true;
        command = "";
        counter = COUNTER_INIT;
        item = "";
        X = -1;
        Y = -1;
        isXWndShown = false;
        isYWndShown = false;
        toBeUsed = null;
        isSelected = false;
        isEqpWndShown = false;
    }

    private void throwItem() {
        if (!isEqpWndShown) {
            isEqpWndShown = true;
            isSelected = false;
            ShatteredPixelDungeon.scene().addToFront(new WndTextInput("What do you want to use?",
                    "",
                    item,
                    50,
                    false,
                    Messages.get(Char.class, "agent_set"),
                    Messages.get(Char.class, "agent_clear")) {
                @Override
                public void onSelect(boolean positive, String text) {
                    if (Objects.equals(text, "")) {
                        GLog.e("Invalid item.");
                        return;
                    }
                    isSelected = true;
                    item = text;
                }
            });
        }

        if (!isSelected) {
            return;
        }

        for (Item i : Dungeon.hero.belongings.getAllItems(Item.class)){
            if (toBeUsed == null && i.toString().equals(item.replace(" ", "_").toLowerCase())) {
                toBeUsed = i;
            }
        }

        for (Item i : Dungeon.hero.belongings.getEquipments()){
            if (toBeUsed == null && i.toString().equals(item.replace(" ", "_").toLowerCase())) {
                toBeUsed = i;
            }
        }

        if (isSelected && toBeUsed == null) {
            isSelected = false;
            isEqpWndShown = false;
            GLog.e(item + " is not in the inventory.");
            return;
        }

        if (!toBeUsed.actions(Dungeon.hero).contains(AC_THROW)) {
            isSelected = false;
            isEqpWndShown = false;
            GLog.e(item + " is not cannot be thrown.");
            return;
        }

        else if (toBeUsed == null) {
            return;
        }

        if (!isXWndShown) {
            isXWndShown = true;
            ShatteredPixelDungeon.scene().addToFront(new WndTextInput("Your Current X is" + currX + ". Enter your X: ",
                    "",
                    String.valueOf(currX),
                    4,
                    false,
                    Messages.get(Char.class, "agent_set"),
                    Messages.get(Char.class, "agent_clear")) {
                @Override
                public void onSelect(boolean positive, String text) {
                    if (Objects.equals(text, "") || Integer.parseInt(text) < 0) {
                        GLog.e("Invalid X.");
                        isXWndShown = false;
                        return;
                    }
                    X = Integer.parseInt(text);
                }
            });
        }

        if (X >= 0 && !isYWndShown) {
            isYWndShown = true;
            ShatteredPixelDungeon.scene().addToFront(new WndTextInput("Your Current Y is" + currY + ". Enter your Y: ",
                    "",
                    String.valueOf(currY),
                    4,
                    false,
                    Messages.get(Char.class, "agent_set"),
                    Messages.get(Char.class, "agent_clear")) {
                @Override
                public void onSelect(boolean positive, String text) {
                    if (Objects.equals(text, "") || Integer.parseInt(text) < 0) {
                        GLog.e("Invalid Y.");
                        isYWndShown = false;
                        return;
                    }
                    Y = Integer.parseInt(text);
                }
            });
        }

        if (X < 0 || Y < 0) {
            return;
        }

        cell = Position.transferXY2Pos(X, Y);

        GLog.w("Throw: " + item + " to: " + Arrays.toString(Position.transferPos2XY(cell)));
        toBeUsed.cast(Dungeon.hero, cell);
//        toBeUsed.execute(Dungeon.hero, AC_THROW);
//
//        GameScene.handleCell(cell);

        Dungeon.isAgentNext = true;
        command = "";
        item = "";
        X = -1;
        Y = -1;
        isXWndShown = false;
        isYWndShown = false;
        counter = COUNTER_INIT;
        isSelected = false;
        toBeUsed = null;
        isEqpWndShown = false;
    }

    private void upgrade() {
        if (!isEqpWndShown) {
            // Check exception
            if (!Dungeon.hero.isAlive()){
                GLog.e("Hero is dead.");

                Dungeon.isAgentNext = true;
                command = "";
                talent = "";
                counter = COUNTER_INIT;
                isSelected = false;
                toBeUpgraded = null;
                isEqpWndShown = false;
                return;
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

                Dungeon.isAgentNext = true;
                command = "";
                talent = "";
                counter = COUNTER_INIT;
                isSelected = false;
                toBeUpgraded = null;
                isEqpWndShown = false;
                return;
            }

            // Show the talent selection window
            isEqpWndShown = true;
            isSelected = false;
            ShatteredPixelDungeon.scene().addToFront(new WndTextInput("Which Talent do you want to upgrade?",
                    Status.getCurrHeroTalents().toString().replace("_", " ").toLowerCase(),
                    talent,
                    40,
                    false,
                    Messages.get(Char.class, "agent_set"),
                    Messages.get(Char.class, "agent_clear")) {
                @Override
                public void onSelect(boolean positive, String text) {
                    if (Objects.equals(text, "")) {
                        GLog.e("Invalid Talent.");
                        return;
                    }
                    isSelected = true;
                    talent = text;
                }
            });
        }

        if (!isSelected) {
            return;
        }

        // Check if the talent exists
        ArrayList<ArrayList<Talent>> allTalents = Status.getHeroTalents();

        outer:
        for (ArrayList<Talent> tier : allTalents){
            for (Talent t : tier){
                if (toBeUpgraded == null && t.name().replace(" ", "_").equalsIgnoreCase(talent.replace(" ", "_"))) {
                    toBeUpgraded = t;
                    break outer;
                }
            }
        }

        if (toBeUpgraded == null) {
            isSelected = false;
            isEqpWndShown = false;
            GLog.e(talent + " does not exist.");
            return;
        }

        toBeUpgraded = null;
        int tierNumber = 0;

        // Check if the talent is available for current tier
        ArrayList<ArrayList<Talent>> currTalents = Status.getCurrHeroTalents();

        outer:
        for (ArrayList<Talent> tier : currTalents){
            tierNumber ++;
            for (Talent t : tier){
                if (toBeUpgraded == null && t.name().replace(" ", "_").equalsIgnoreCase(talent.replace(" ", "_"))) {
                    toBeUpgraded = t;
                    break outer;
                }
            }
        }

        if (toBeUpgraded == null) {
            isSelected = false;
            isEqpWndShown = false;
            GLog.e(talent + " is not available for current hero tier.");
            return;
        }

        // Check if the talent points in the tier is available
        if (Dungeon.hero.talentPointsAvailable(tierNumber) <= 0) {
            isSelected = false;
            isEqpWndShown = false;
            GLog.e(talent + " cannot be upgraded since not enough point in tier " + tierNumber + ".");
            return;
        }

        // Check if the talent is already maxed
        if (Dungeon.hero.pointsInTalent(toBeUpgraded) >= toBeUpgraded.maxPoints()) {
            isSelected = false;
            isEqpWndShown = false;
            GLog.e(talent + " cannot be upgraded since talent it is already maxed.");
            return;
        }

        GLog.w("Upgrade: " + talent);
        Dungeon.hero.upgradeTalent(toBeUpgraded);

        Dungeon.isAgentNext = true;
        command = "";
        talent = "";
        counter = COUNTER_INIT;
        isSelected = false;
        toBeUpgraded = null;
        isEqpWndShown = false;
    }

    // ==================================================================================
    // =============================== Helper Functions =================================
    // ==================================================================================
}

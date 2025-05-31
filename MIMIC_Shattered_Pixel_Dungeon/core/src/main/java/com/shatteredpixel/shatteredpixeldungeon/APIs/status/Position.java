package com.shatteredpixel.shatteredpixeldungeon.APIs.status;


import com.shatteredpixel.shatteredpixeldungeon.Dungeon;

public class Position {

    public static int[] getHeroPositionXY() {
        int[] position = new int[2];
        position[0] = Dungeon.hero.pos % Dungeon.level.width();
        position[1] = Dungeon.hero.pos / Dungeon.level.width();
        return position;
    }

    public static int getHeroPosition() {
        return Dungeon.hero.pos;
    }

    public static int transferXY2Pos(int x, int y) {
        return x + y * Dungeon.level.width();
    }

    public static int transferXY2Pos(int[] xy) {
        return xy[0] + xy[1] * Dungeon.level.width();
    }

    public static int[] transferPos2XY(int pos) {
        int[] position = new int[2];
        position[0] = pos % Dungeon.level.width();
        position[1] = pos / Dungeon.level.width();
        return position;
    }
}

package com.codecool.dungeoncrawl.gui;

import com.codecool.dungeoncrawl.logic.Drawable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.Map;

public class Tiles {
    public static int TILE_WIDTH = 32;
    public static int TILE_HIGHT= 32;

    private static Image tileset = new Image("/tiles.png", 543 * 2, 543 * 2, true, false);
    private static Map<String, Tile> tileMap = new HashMap<>();
    public static class Tile {
        public final int x, y, w, h;
        Tile(int i, int j) {
            x = i * (TILE_WIDTH + 2);
            y = j * (TILE_HIGHT + 2);
            w = TILE_WIDTH;
            h = TILE_HIGHT;
        }
    }

    static {
        tileMap.put("empty", new Tile(0, 0));
        tileMap.put("wall", new Tile(10, 17));
        tileMap.put("floor", new Tile(2, 0));
        tileMap.put("player", new Tile(27, 0));
        tileMap.put("backbone", new Tile(29, 6));
        tileMap.put("ghost", new Tile(26, 6));
        tileMap.put("defender", new Tile(31, 0));
        tileMap.put("axe", new Tile(8, 29));
        tileMap.put("key", new Tile(17, 23));
        tileMap.put("shield", new Tile(8, 24));
        tileMap.put("closeDoor", new Tile(0, 9));
        tileMap.put("openDoor", new Tile(2, 9));
        tileMap.put("stairsDown", new Tile(4, 6));
        tileMap.put("stairsUp", new Tile(4, 7));
        tileMap.put("potion", new Tile(16, 25));
        tileMap.put("win", new Tile(12, 24));
        tileMap.put("crown", new Tile(11, 24));
        tileMap.put("boss", new Tile(30, 6));
    }

    public static void drawTile(GraphicsContext context, Drawable d, int x, int y) {
        Tile tile = tileMap.get(d.getTileName());
        context.drawImage(tileset, tile.x, tile.y, tile.w, tile.h,
                x * TILE_WIDTH, y * TILE_WIDTH, TILE_WIDTH, TILE_WIDTH);
    }
}

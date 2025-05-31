package com.shatteredpixel.shatteredpixeldungeon.APIs.status;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundlable;
import com.watabou.utils.PathFinder;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

public class Environment {

    public static Tile[][] currTiles = getTilesXY();

    public static String terrain2Str(int terrain) {
        if (terrain == 0) return "chasm";
        if (terrain == 1) return "empty";
        if (terrain == 2) return "grass";
        if (terrain == 3) return "empty_well";
        if (terrain == 4) return "wall";
        if (terrain == 5) return "door";
        if (terrain == 6) return "open_door";
        if (terrain == 7) return "entrance";
        if (terrain == 8) return "exit";
        if (terrain == 9) return "embers";
        if (terrain == 10) return "locked_door";
        if (terrain == 31) return "crystal_door";
        if (terrain == 11) return "pedestal";
        if (terrain == 12) return "wall_deco";
        if (terrain == 13) return "barricade";
        if (terrain == 14) return "empty_sp";
        if (terrain == 15) return "high_grass";
        if (terrain == 30) return "furrowed_grass";

        if (terrain == 16) return "secret_door";
        if (terrain == 17) return "secret_trap";
        if (terrain == 18) return "trap";
        if (terrain == 19) return "inactive_trap";

        if (terrain == 20) return "empty_deco";
        if (terrain == 21) return "locked_exit";
        if (terrain == 22) return "unlocked_exit";
        if (terrain == 24) return "well";
        if (terrain == 27) return "bookshelf";
        if (terrain == 28) return "alchemy";

        if (terrain == 32) return "custom_deco_empty";
        if (terrain == 23) return "custom_deco";
        if (terrain == 25) return "statue";
        if (terrain == 26) return "statue_sp";
        if (terrain == 35) return "mine_crystal";
        if (terrain == 36) return "mine_boulder";

        if (terrain == 29) return "water";

        return "unknown";
    }

    public static int getWidth() {
        return Dungeon.level.width();
    }

    public static int getHeight() {
        return Dungeon.level.height();
    }

    public static int getMapSize() {
        return Dungeon.level.map.length;
    }

    public static int getTile(int pos) {
        return Dungeon.level.map[pos];
    }

    public static int getTile(int x, int y) {
        return Dungeon.level.map[x + y * Dungeon.level.width()];
    }

    public static String[] getMap() {
        String[] map = new String[Dungeon.level.map.length];

        for (int i = 0; i < Dungeon.level.map.length; i++) {
            map[i] = terrain2Str(Dungeon.level.map[i]);
        }

        return map;
    }

    public static String[][] getMapXY() {
        String[][] map = new String[Dungeon.level.height()][Dungeon.level.width()];

        for (int y = 0; y < Dungeon.level.height(); y++) {
            for (int x = 0; x < Dungeon.level.width(); x++) {
                map[y][x] = terrain2Str(Dungeon.level.map[x + y * Dungeon.level.width()]);
            }
        }

        return map;
    }

    public static boolean[] getVisitedMap() {
        return Dungeon.level.visited;
    }

    public static boolean[][] getVisitedMapXY() {
        boolean[][] map = new boolean[Dungeon.level.height()][Dungeon.level.width()];

        for (int y = 0; y < Dungeon.level.height(); y++) {
            for (int x = 0; x < Dungeon.level.width(); x++) {
                map[y][x] = Dungeon.level.visited[x + y * Dungeon.level.width()];
            }
        }

        return map;
    }

    public static boolean[] getMapped() {
        return Dungeon.level.mapped;
    }

    public static boolean[][] getMappedXY() {
        boolean[][] map = new boolean[Dungeon.level.height()][Dungeon.level.width()];

        for (int y = 0; y < Dungeon.level.height(); y++) {
            for (int x = 0; x < Dungeon.level.width(); x++) {
                map[y][x] = Dungeon.level.mapped[x + y * Dungeon.level.width()];
            }
        }

        return map;
    }

    public static boolean[] getDiscoverableMap() {
        return Dungeon.level.discoverable;
    }

    public static boolean[][] getDiscoverableMapXY() {
        boolean[][] map = new boolean[Dungeon.level.height()][Dungeon.level.width()];

        for (int y = 0; y < Dungeon.level.height(); y++) {
            for (int x = 0; x < Dungeon.level.width(); x++) {
                map[y][x] = Dungeon.level.discoverable[x + y * Dungeon.level.width()];
            }
        }

        return map;
    }

    public static boolean[] getHeroFOV() {
        return Dungeon.level.heroFOV;
    }

    public static boolean[][] getHeroFOVXY() {
        boolean[][] HeroFOV = new boolean[Dungeon.level.height()][Dungeon.level.width()];

        for (int y = 0; y < Dungeon.level.height(); y++) {
            for (int x = 0; x < Dungeon.level.width(); x++) {
                HeroFOV[y][x] = Dungeon.level.heroFOV[x + y * Dungeon.level.width()];
            }
        }

        return HeroFOV;
    }

    /**
     * Get the tiles with the objects on them
     * @return Tile[] with the objects on them
     */
    public static Tile[] getTiles() {
        Tile[] tiles = new Tile[Dungeon.level.map.length];

        for (int i = 0; i < Dungeon.level.map.length; i++) {
            tiles[i] = new Tile(i);
        }

        // Update Hero's position
        tiles[Dungeon.hero.pos].obj = Dungeon.hero;

        // Update Mobs' positions
        for (Mob mob : Dungeon.level.mobs) {
            tiles[mob.pos].obj = mob;
        }

        // Update Heaps' (items or interaction heaps) positions
        for (Heap heap : Dungeon.level.heaps.valueList()) {
            if (tiles[heap.pos].obj == null)
                tiles[heap.pos].obj = heap.type.equals(Heap.Type.HEAP) ? heap.peek() : heap;
            else
                tiles[heap.pos].obj2 = heap.type.equals(Heap.Type.HEAP) ? heap.peek() : heap;
        }

        // TODO: Figure out from Blob and Rect; Seems to be like an area for the buff / debuff
//        // Update Blobs' positions
//        for (Blob blob : Dungeon.level.blobs.values()) {
//            tiles[blob.pos].obj = Dungeon.level.blobs.get(blob.pos);
//        }

        // Update Plants' positions
        for (Plant plant : Dungeon.level.plants.valueList()) {
            tiles[plant.pos].obj = plant;
        }

        // Update Traps' positions
        for (int pos : Dungeon.level.traps.keyArray()) {
            tiles[pos].obj = Dungeon.level.traps.get(pos);
        }

        return tiles;
    }

    /**
     * Get the tiles with the objects on them
     * @return Tile[][] with the objects on them
     */
    public static Tile[][] getTilesXY() {
        Tile[][] tiles = new Tile[Dungeon.level.height()][Dungeon.level.width()];

        for (int y = 0; y < Dungeon.level.height(); y++) {
            for (int x = 0; x < Dungeon.level.width(); x++) {
                tiles[y][x] = new Tile(x, y);
            }
        }

        // Update Hero's position
        tiles[Position.getHeroPositionXY()[1]][Position.getHeroPositionXY()[0]].obj = Dungeon.hero;

        // Update Mobs' positions
        for (Mob mob : Dungeon.level.mobs) {
            tiles[Position.transferPos2XY(mob.pos)[1]][Position.transferPos2XY(mob.pos)[0]].obj = mob;
        }

        // Update Heaps' (items or interaction heaps) positions
        for (Heap heap : Dungeon.level.heaps.valueList()) {
            if (tiles[Position.transferPos2XY(heap.pos)[1]][Position.transferPos2XY(heap.pos)[0]].obj == null)
                tiles[Position.transferPos2XY(heap.pos)[1]][Position.transferPos2XY(heap.pos)[0]].obj = heap.type.equals(Heap.Type.HEAP) ? heap.peek() : heap;
            else
                tiles[Position.transferPos2XY(heap.pos)[1]][Position.transferPos2XY(heap.pos)[0]].obj2 = heap.type.equals(Heap.Type.HEAP) ? heap.peek() : heap;
        }

        // TODO: Figure out from Blob and Rect; Seems to be like an area for the buff / debuff
//        // Update Blobs' positions
//        for (Blob blob : Dungeon.level.blobs.values()) {
//            tiles[Position.transferPos2XY(blob.pos)[1]][Position.transferPos2XY(blob.pos)[0]].obj = Dungeon.level.blobs.get(blob.pos);
//        }

        // Update Plants' positions
        for (Plant plant : Dungeon.level.plants.valueList()) {
            tiles[Position.transferPos2XY(plant.pos)[1]][Position.transferPos2XY(plant.pos)[0]].obj = plant;
        }

        // Update Traps' positions
        for (int pos : Dungeon.level.traps.keyArray()) {
            tiles[Position.transferPos2XY(pos)[1]][Position.transferPos2XY(pos)[0]].obj = Dungeon.level.traps.get(pos);
        }

        return tiles;
    }

    /**
     * Get the visible tiles
     * @return String[] with the visible tiles
     */
    public static String[] getVisibleTiles() {
        Tile[] tiles = getTiles();
        String[] visibleTiles = new String[tiles.length];

        // If the tile is visited, the block shows with fog if hero is away; fog covers only mobs
        for (int i = 0; i < tiles.length; i++) {
            visibleTiles[i] = tiles[i].visited ? tiles[i].toString() : "unknown";
        }

        // If the tile is in hero's field of view; the tiles without FOV will be covered with fog; fog covers only mobs
        for (int i = 0; i < tiles.length; i++) {
            if (visibleTiles[i].equals("unknown"))
                continue;

            // If the tile is not in FOV, hide the mobs
            if (!tiles[i].heroFOV && tiles[i].obj instanceof Char) {
                // If no items on this tile
                if (tiles[i].obj2 == null) {
                    visibleTiles[i] = tiles[i].terrain;

                    // If there are items on this tile, only hide the mobs
                } else {
                    visibleTiles[i] = "(" + tiles[i].terrain + ", " + tiles[i].obj2 + ")";
                }

            } else {
                visibleTiles[i] = tiles[i].toString();
            }
        }

        return visibleTiles;
    }

    /**
     * Get the visible tiles
     * @return String[][] with the visible tiles
     */
    public static String[][] getVisibleTilesXY() {
        currTiles = getTilesXY();
        String[][] visibleTiles = new String[currTiles.length][currTiles[0].length];

        // If the tile is visited, the block shows with fog if hero is away; fog covers only mobs
        for (int y = 0; y < currTiles.length; y++) {
            for (int x = 0; x < currTiles[0].length; x++) {
                if (currTiles[y][x].visited) {
                    visibleTiles[y][x] = currTiles[y][x].toString();
                } else {
                    visibleTiles[y][x] = "unknown";
                }
            }
        }

        // If the tile is in hero's field of view; the tiles without FOV will be covered with fog; fog covers only mobs
        for (int y = 0; y < currTiles.length; y++) {
            for (int x = 0; x < currTiles[0].length; x++) {
                if (visibleTiles[y][x].equals("unknown"))
                    continue;

                // If the tile is not in FOV, hide the mobs
                if (!currTiles[y][x].heroFOV && currTiles[y][x].obj instanceof Char) {
                    // If no items on this tile
                    if (currTiles[y][x].obj2 == null) {
                        visibleTiles[y][x] = currTiles[y][x].terrain;

                    // If there are items on this tile, only hide the mobs
                    } else {
                        visibleTiles[y][x] = "(" + currTiles[y][x].terrain + ", " + currTiles[y][x].obj2 + ")";
                    }

                } else {
                    visibleTiles[y][x] = currTiles[y][x].toString();
                }
            }
        }

        return visibleTiles;
    }

    /**
     * Get the important tiles in JSON format; the important tiles are the tiles with the objects on them or the important terrains.
     * The unimportant terrains are:
     * empty, grass, empty_well, wall, wall_deco, empty_sp, empty_deco, custom_deco_empty, and water.
     * @return ArrayList<JSONObject> with the important tiles in pos
     */
    public static ArrayList<JSONObject> getImportantTilesJSON() {
        String[] tiles = getVisibleTiles();
        ArrayList<JSONObject> importantTiles = new ArrayList<>();

        for (int pos = 0; pos < tiles.length; pos++) {

                // 1. The tile for shown should never be unknown
                if (tiles[pos].contains("unknown")) {
                    continue;
                }

                // 2. If the tile is unreachable and no object on it, it is not important
                if (tiles[pos].contains("unreachable") && tiles[pos].contains("null")) {
                    continue;
                }

                // 3. if a tile is a misleading terrain, it is not important
                if (tiles[pos].contains("(secret_door,") ||
                        tiles[pos].contains("(secret_trap,") ||
                        tiles[pos].contains("(bookshelf,") ||
                        tiles[pos].contains("(alchemy,") ) {
                    continue;
                }

                // 4. If a tile is not an important terrain,
                // and neither has object on it nor is boundary, it is not important
                if (tiles[pos].contains("null") && !tiles[pos].contains("[is boundary]") && (
                        tiles[pos].contains("(empty,") ||
                        tiles[pos].contains("(empty_well,") ||
                        tiles[pos].contains("(wall,") ||
                        tiles[pos].contains("(wall_deco,") ||
                        tiles[pos].contains("(empty_sp,") ||
                        tiles[pos].contains("(empty_deco,") ||
                        tiles[pos].contains("(custom_deco_empty,") ||
                        tiles[pos].contains("(water,)")) ) {
                    continue;
                }

                JSONObject tile = new JSONObject();

                try {
                    tile.put(tiles[pos], pos);

                } catch (Exception e) {
                    throw new JSONException(e);
                }

                importantTiles.add(tile);
        }

        return importantTiles;
    }

    /**
     * Get the important tiles in JSON format; the important tiles are the tiles with the objects on them or the important terrains or the boundary of an unexplored area.
     * The unimportant terrains are:
     * empty, empty_well, wall, wall_deco, empty_sp, empty_deco, and custom_deco_empty.
     * @return ArrayList<JSONObject> with the important tiles in XY
     */
    public static ArrayList<JSONObject> getImportantTilesXYJSON() {
        String[][] tiles = getVisibleTilesXY();
        ArrayList<JSONObject> importantTiles = new ArrayList<>();

        for (int y = 0; y < tiles.length; y++) {
            for (int x = 0; x < tiles[0].length; x++) {

                // 1. The tile for showing should never be unknown
                if (tiles[y][x].contains("unknown")) {
                    continue;
                }

                // 2. if a tile is a misleading terrain, it is not important
                if (tiles[y][x].contains("(secret_door,") ||
                        tiles[y][x].contains("(secret_trap,") ||
                        tiles[y][x].contains("(bookshelf,") ||
                        tiles[y][x].contains("(alchemy,") ) {
                    continue;
                }

                // 3. If the tile is unreachable and no object on it, and not important terrain, it is not important
                if (tiles[y][x].contains("unreachable") && tiles[y][x].contains("null") && (
                        tiles[y][x].contains("(empty,") ||
                                tiles[y][x].contains("(empty_well,") ||
                                tiles[y][x].contains("(wall,") ||
                                tiles[y][x].contains("(wall_deco,") ||
                                tiles[y][x].contains("(empty_sp,") ||
                                tiles[y][x].contains("(empty_deco,") ||
                                tiles[y][x].contains("(custom_deco_empty,"))) {
                    continue;
                }

                // 4. If a tile is not an important terrain,
                // and neither has object on it nor is boundary, it is not important
                if (tiles[y][x].contains("null") && !tiles[y][x].contains("[is boundary]") && (
                        tiles[y][x].contains("(empty,") ||
                        tiles[y][x].contains("(empty_well,") ||
                        tiles[y][x].contains("(wall,") ||
                        tiles[y][x].contains("(wall_deco,") ||
                        tiles[y][x].contains("(empty_sp,") ||
                        tiles[y][x].contains("(empty_deco,") ||
                        tiles[y][x].contains("(custom_deco_empty,"))) {
                    continue;
                }

                JSONObject tile = new JSONObject();

                try {
                    tile.put(tiles[y][x], new int[]{x, y});

                } catch (Exception e) {
                    throw new JSONException(e);
                }

                importantTiles.add(tile);
            }
        }

        return importantTiles;
    }

    public static boolean isReachable(Tile tile) {
        if(Dungeon.hero == null || Dungeon.hero.fieldOfView == null) {
            return true;
        }

        // Get the hero's position
        int pos = Position.getHeroPosition();

        // Get target position
        int target = tile.pos;

        if (Dungeon.level.adjacent( pos, target ) || pos == target) {
            return true;
        }

        int len = Dungeon.level.length();
        boolean[] p = Dungeon.level.passable;
        boolean[] v = Dungeon.level.visited;
        boolean[] m = Dungeon.level.mapped;
        boolean[] passable = new boolean[len];
        for (int i = 0; i < len; i++) {
            passable[i] = p[i] && (v[i] || m[i]);
        }

        PathFinder.Path newpath = Dungeon.findPath(Dungeon.hero, target, passable, Dungeon.hero.fieldOfView, true);

        return newpath != null;
    }

    /**
     * Check if the tile is the boundary of the unexplored area
     * A tile is the boundary of the unexplored area if
     * 1) It is not a wall or wall_deco
     * 2) Any of the surrounding tile is unexplored and not a wall or wall_deco
     *
     * @param tile the tile to be checked
     * @return true if the tile is the boundary of the unexplored area; false otherwise
     */
    public static boolean isBoundary(Tile tile) {
        if (tile.terrain.equals("wall") || tile.terrain.equals("wall_deco")) {
            return false;
        }

        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

        int x = tile.x;
        int y = tile.y;

        for (int[] direction : directions) {
            int nextX = x + direction[0];
            int nextY = y + direction[1];

            if (nextX >= 0 && nextX < Dungeon.level.width() && nextY >= 0 && nextY < Dungeon.level.height()) {
                Tile nextTile = currTiles[nextY][nextX];

                if (nextTile.discoverable &&
                        !nextTile.terrain.equals("wall") &&
                        !nextTile.terrain.equals("wall_deco") &&
                        !nextTile.visited) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String getBlobInMap(int tileX, int tileY) {
        Collection<Blob> blobs = Dungeon.level.blobs.values();

        for (Blob blob : blobs) {
            if (blob.area != null) {
                for (int x = blob.area.left; x <= blob.area.right; x++) {
                    for (int y = blob.area.top; y <= blob.area.bottom; y++) {
                        if (x == tileX && y == tileY) {
                            return blob.toString();
                        }
                    }
                }
            }
        }

        return null;
    }

    public static class Tile {

        public int x;
        public int y;
        public int pos;

        public boolean visible;

        // If the tile is visited, the block shows with fog if hero is away; fog covers only mobs
        public boolean visited;

        // If the tile is in hero's field of view; the tiles without FOV will be covered with fog; fog covers only mobs
        public boolean heroFOV;

        // TODO: To be figured out
        public boolean mapped;

        // If the tile is discoverable; meaning if it is inside the walls or not
        // TODO: Check it before act to the tile
        public boolean discoverable;

        public Bundlable obj;
        public Bundlable obj2;

        public String terrain;
        public String blob;

        public Tile(int pos) {
            this.pos = pos;
            this.x = pos % Dungeon.level.width();
            this.y = pos / Dungeon.level.width();

            this.visited = Dungeon.level.visited[pos];
            this.mapped = Dungeon.level.mapped[pos];
            this.discoverable = Dungeon.level.discoverable[pos];
            this.heroFOV = Dungeon.level.heroFOV[pos];

            this.terrain = terrain2Str(Dungeon.level.map[pos]);
            this.blob = getBlobInMap(this.x, this.y);
        }

        public Tile(int x, int y) {
            this.x = x;
            this.y = y;
            this.pos = x + y * Dungeon.level.width();

            this.visited = Dungeon.level.visited[pos];
            this.mapped = Dungeon.level.mapped[pos];
            this.discoverable = Dungeon.level.discoverable[pos];
            this.heroFOV = Dungeon.level.heroFOV[pos];

            this.terrain = terrain2Str(Dungeon.level.map[pos]);
            this.blob = getBlobInMap(this.x, this.y);

            if (this.terrain.equals("entrance") || this.terrain.equals("exit") || this.terrain.equals("locked_exit")) {
                LevelTransition lt = Level.getTransition(pos);
                switch (Objects.requireNonNull(lt).type) {
                    case SURFACE:
                        this.terrain += " to the surface";
                        break;
                    case REGULAR_ENTRANCE:
                        this.terrain += " to the previous level";
                        break;
                    case REGULAR_EXIT:
                        this.terrain += " to the next level";
                        break;
                    case BRANCH_ENTRANCE:
                        this.terrain += " to the branch";
                        break;
                    case BRANCH_EXIT:
                        this.terrain += " out of the branch";
                        break;
                }
            }
        }

        @Override
        public String toString() {
            String str = "(" + terrain;

            if (blob != null) {
                str += " [" + blob + "]";
            }

            if (obj == null) {
                str += ", null";
            } else {
                str += ", " + obj;
            }

            if (obj2 == null) {
                str += ")";
            } else {
                str += ", " + obj2 + ")";
            }

            if (isBoundary(this)) {
                str += " [is boundary]";
            }

            if (!isReachable(this)) {
                str += " [unreachable]";
            }

            return str;
        }
    }
}

package com.codecool.dungeoncrawl.APIs.status;

import com.codecool.dungeoncrawl.agent.GLog;
import com.codecool.dungeoncrawl.logic.map.Cell;
import com.codecool.dungeoncrawl.logic.map.CellType;
import com.codecool.dungeoncrawl.logic.map.GameMap;
import com.codecool.dungeoncrawl.gui.Main;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.codecool.dungeoncrawl.agent.skill_library.basic_skills.Navigate.findPathIgnoreMobs;
import static com.codecool.dungeoncrawl.agent.skill_library.basic_skills.Utils.isValid;
import static com.codecool.dungeoncrawl.gui.Main.map;

public class Environment {
    public static int getWidth() {
        return map.getWidth();
    }

    public static int getHeight() {
            return map.getHeight();
    }

    public static Cell getTile(int x, int y) {
        return map.getCell(x, y);
    }

    private static ArrayList<Cell[][]> mapMemorys = new ArrayList<>();
    private static ArrayList<boolean[][]> visitedMaps = new ArrayList<>();

    private static Cell[][] mapMemory = new Cell[map.getHeight()][map.getWidth()];
    private static boolean[][] visitedMap = new boolean[map.getHeight()][map.getWidth()];

    public static void newLevelRefresh() {
        if (Main.level >= mapMemorys.size()) {
            mapMemory = new Cell[map.getHeight()][map.getWidth()];
            visitedMap = new boolean[map.getHeight()][map.getWidth()];

            mapMemorys.add(mapMemory);
            visitedMaps.add(visitedMap);
        } else {
            mapMemory = mapMemorys.get(Main.level);
            visitedMap = visitedMaps.get(Main.level);
        }
    }

    public static void updateVisitedMap() {
        Cell playerCell = map.getPlayer().getCell();

        for (int x = playerCell.getX() - 10; x <= playerCell.getX() + 10; x++) {
            for (int y = playerCell.getY() - 10; y <= playerCell.getY() + 10; y++) {
                if (x >= 0 && x < map.getWidth() && y >= 0 && y < map.getHeight()) {
                    visitedMap[y][x] = true;
                }
            }
        }

        mapMemory = new Cell[map.getHeight()][map.getWidth()];

        try {
            for (int x = 0; x < map.getWidth(); x++) {
                for (int y = 0; y < map.getHeight(); y++) {
                    if (visitedMap[y][x]) {
                        mapMemory[y][x] = map.getCell(x, y);
                    } else {
                        mapMemory[y][x] = new Cell(map, x, y, CellType.UNKNOWN);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static Cell[][] getMapMemory() {
        updateVisitedMap();
        return mapMemory;
    }

    public static String getMapMemoryStr() {
        return Arrays.deepToString(getMapMemory());
    }

    public static boolean[][] getVisitedMap() {
        updateVisitedMap();
        return visitedMap;
    }

    public static String getVisitedMapStr() {
        return Arrays.deepToString(getVisitedMap());
    }

    public static Cell[][] getMap() {
        Cell[][] cells = new Cell[map.getHeight()][map.getWidth()];

        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                cells[y][x] = map.getCell(x, y);
            }
        }

        return cells;
    }

    public static String getMapStr() {
        return Arrays.deepToString(getMap());
    }

    public static Cell[][] getMapVisibleMap() {
        Cell playerCell = map.getPlayer().getCell();
        Cell[][] visibleCells = new Cell[21][21];

        for (int x = playerCell.getX() - 10; x <= playerCell.getX() + 10; x++) {
            for (int y = playerCell.getY() - 10; y <= playerCell.getY() + 10; y++) {
                if (x >= 0 && x < map.getWidth() && y >= 0 && y < map.getHeight()) {
                    visibleCells[y - playerCell.getY() + 10][x - playerCell.getX() + 10] = map.getCell(x, y);
                } else {
                    visibleCells[y - playerCell.getY() + 10][x - playerCell.getX() + 10] = new Cell(map, x, y, CellType.EMPTY);
                }
            }
        }
        return visibleCells;
    }

    public static String getMapVisibleMapStr() {
        return Arrays.deepToString(getMapVisibleMap());
    }

    /**
     * Get the visible map with 2 cells more in each direction as memory
     * @return
     */
    public static Cell[][] getMapVisibleMapWithMemory() {
        Cell playerCell = map.getPlayer().getCell();
        Cell[][] visibleCells = new Cell[25][25];
        boolean[][] visitedMap = getVisitedMap();

        for (int x = playerCell.getX() - 12; x <= playerCell.getX() + 12; x++) {
            for (int y = playerCell.getY() - 12; y <= playerCell.getY() + 12; y++) {

                 if (isValid(x, y)) {
                     if (visitedMap[y][x])
                         visibleCells[y - playerCell.getY() + 12][x - playerCell.getX() + 12] = map.getCell(x, y);
                     else
                         visibleCells[y - playerCell.getY() + 12][x - playerCell.getX() + 12] = new Cell(map, x, y, CellType.UNKNOWN);

                 } else visibleCells[y - playerCell.getY() + 12][x - playerCell.getX() + 12] = new Cell(map, x, y, CellType.EMPTY);
            }
        }
        return visibleCells;
    }

    public static String getMapVisibleMapWithMemoryStr() {
        return Arrays.deepToString(getMapVisibleMapWithMemory());
    }

    public static ArrayList<JSONObject> getImportantCellsJSONMemory() {
        Cell[][] cells = getMapMemory();
        ArrayList<JSONObject> importantTiles = new ArrayList<>();

        for (Cell[] row : cells) {
            for (Cell cell : row) {
                if (cell.getType() != CellType.EMPTY) {
                    JSONObject tile = new JSONObject();

                    try {
                        tile.put(cell.toString(), cell.getCellPosStr());

                    } catch (Exception e) {
                        GLog.e("Error in getImportantCellsJSONMemory: " + e.getMessage());
                    }

                    importantTiles.add(tile);
                }
            }
        }

        return importantTiles;
    }

    /**
     * If this tile is adjacent to any of the unknown tile, then it is a tile to explore
     * @param x The x coordinate of the tile in the visible map
     * @param y The y coordinate of the tile in the visible map
     * @return True if the tile is to explore, false otherwise
     */
    public static boolean isTileToExplore(int x, int y) {
        Cell[][] cells = getMapVisibleMapWithMemory();

        if (isValid(x, y) && cells[y][x].getType() != CellType.UNKNOWN) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    int newX = x + i;
                    int newY = y + j;

                    if (isValid(newX, newY) &&
                            newX >= 0 && newX < cells[0].length && newY >= 0 && newY < cells.length &&
                            cells[newY][newX].getType() == CellType.UNKNOWN) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Get the important cells in the visible map
     * 1) Actor, Item, Door, Stairs, and WinObject
     * 2) The cells for exploration -
     *      a) Tiles adjacent to the unknown cells,
     *      b) If no such tiles, then the tile on the path to the stairsDown
     *
     * @return The important cells in the visible map
     */
    public static ArrayList<JSONObject> getImportantCellsJSON() {
        Cell[][] cells = getMapVisibleMapWithMemory();
        ArrayList<JSONObject> importantTiles = new ArrayList<>();

        boolean hasTileForExplore = false;

        for (int y = 0; y < cells.length; y++) {
            for (int x = 0; x < cells[y].length; x++) {
                Cell cell = cells[y][x];

                if ( (cell.getType() != CellType.EMPTY && cell.getType() != CellType.FLOOR && cell.getType() != CellType.WALL && cell.getType() != CellType.UNKNOWN && cell.getType() != CellType.FAKEWIN) ||
                        cell.getActor() != null ||
                        cell.isItemOnCell() ||
                        isTileToExplore( x, y ) ) {

                    JSONObject tile = new JSONObject();

                    try {
                        String info = cell.toString();

                        if ( isTileToExplore( x, y ) ) {
                            info += " [For Exploration]";
                            hasTileForExplore = true;

                        }

                        tile.put( cell.getCellPosStr(), info );

                        if (!info.toLowerCase().contains("empty") && !info.toLowerCase().contains("wall")) {
                            importantTiles.add(tile);
                        }

                    } catch (Exception e) {
                        GLog.e("Error in getImportantCellsJSON: " + e.getMessage());
                    }

                }
            }
        }

        if ( !hasTileForExplore ) {

            Cell cell = null;

            // If stairsDown is in the visible map, skip this step
            for (Cell[] value : cells) {
                for (Cell item : value) {
                    if (item.getType() == CellType.STAIRSDOWN) {
                        return importantTiles;
                    }
                }
            }

            // Find the stairsDown cell
            OUTER:
            for (int y = 0; y < map.getHeight(); y++) {
                for (int x = 0; x < map.getWidth(); x++) {
                    cell = map.getCell(x, y);

                    if ( cell.getType() == CellType.STAIRSDOWN ) {
                        break OUTER;
                    }
                }
            }

            assert cell != null;
            List<Cell> path = findPathIgnoreMobs(cell);

            int xPadding = cells[0][0].getX();
            int yPadding = cells[0][0].getY();

            // Get the tile on the edge of the visible map and on the path to the stairsDown
            for ( Cell currCell: path ) {
                int currX = currCell.getX();
                int currY = currCell.getY();

                if ( (currX == xPadding + 2 && currY >= yPadding + 2 && currY <= yPadding + 23) ||
                        (currX == xPadding + 22 && currY >= yPadding + 2 && currY <= yPadding + 22) ||
                        (currY == yPadding + 2 && currX >= xPadding + 2 && currX <= xPadding + 22) ||
                        (currY == yPadding + 22 && currX >= xPadding + 2 && currX <= xPadding + 22) ) {

                    JSONObject tile = new JSONObject();

                    try {
                        tile.put( currCell.getCellPosStr(), currCell.toString() + " [For Exploration]" );
                        importantTiles.add(tile);

                    } catch (Exception e) {
                        GLog.e("Error in getImportantCellsJSON: " + e.getMessage());
                    }
                }

            }
        }

        return importantTiles;
    }
}

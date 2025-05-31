package com.codecool.dungeoncrawl.logic.map;

public enum CellType {
    EMPTY("empty"),
    FLOOR("floor"),
    CLOSE("closeDoor"),
    STAIRSDOWN("stairsDown"),
    STAIRSUP("stairsUp"),
    OPEN("openDoor"),
    WIN("win"),
    FAKEWIN("crown"),
    WALL("wall"),
    UNKNOWN("unknown");

    private final String tileName;

    CellType(String tileName) {
        this.tileName = tileName;
    }

    public String getTileName() {
        return tileName;
    }
}

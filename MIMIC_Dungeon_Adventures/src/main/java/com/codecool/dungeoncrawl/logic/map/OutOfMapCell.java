package com.codecool.dungeoncrawl.logic.map;

import com.codecool.dungeoncrawl.logic.Drawable;

public class OutOfMapCell implements Drawable {

    @Override
    public String getTileName() {
        return "empty";
    }
}

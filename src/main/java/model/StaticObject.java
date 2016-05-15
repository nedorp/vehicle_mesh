package model;

import places.Position;

/**
 * Super class for static entities (e.g. Stations)
 */
public class StaticObject {

    Position position;

    public StaticObject(Position passPos){
        position = passPos;
    }
}

package model;

import places.Position;

/**
 * Created by Luigi on 07/05/2016.
 */
public interface MovementEngine {

    /** Movement function, random or along roads according to implementation
     * It returns the new position */
    void move(Position objectPosition, double speed);
}

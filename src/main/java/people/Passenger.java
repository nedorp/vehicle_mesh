package people;

import model.MovingObject;
import places.Position;
import simulation.StreetGrid;

public class Passenger extends MovingObject{

    private final static int MOVEMENT_INTERVAL = 500;

    String id;
    float weight;
    Position destination;

    /**
     * Constructor
     *
     * @param movementInterval time between two movement
     * @param godModule        the module that is delegated to move this object
     * @param myPosition        position of the passenger
     */
    public Passenger(StreetGrid godModule, Position myPosition) {
        super(MOVEMENT_INTERVAL, godModule, myPosition);
    }
}

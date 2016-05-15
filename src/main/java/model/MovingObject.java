package model;

import places.Position;
import simulation.StreetGrid;
import utility.MathOperation;

import static utility.MathOperation.*;

public class MovingObject {

    // Line Trajectory, coordinates of the normalized vector
    double trajectX;
    double trajectY;
    double speed;
    double acceleration;

    // The position in the world
    public Position position;

    // MovementEngine
    private MovementEngine motor;

    /**
     * Constructor
     * @param movementInterval time between two movement
     * @param godModule the module that is delegated to move this object
     * @param position initial position of the object
     */
    public MovingObject(int movementInterval, StreetGrid godModule, Position position){

        // Register the object in order to get moved each movementInterval seconds
        godModule.registerObject(this,movementInterval);

        // Set the position
        this.position = position;

    }

    public void imposeTrajectory(Position destination){

        double distance = distance(position,destination);

        trajectX = (destination.getCoordX() - position.getCoordX())/distance;   // normalized
        trajectY = (destination.getCoordY() - position.getCoordY())/distance;   // normalized

    }
}

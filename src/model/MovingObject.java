package model;

import places.Position;
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

    public void imposeTrajectory(Position destination){

        double distance = distance(position,destination);

        trajectX = (destination.getCoordX() - position.getCoordX())/distance;   // normalized
        trajectY = (destination.getCoordY() - position.getCoordY())/distance;   // normalized

    }
}

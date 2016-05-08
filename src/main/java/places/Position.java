package places;

import transport.Car;

/** Useful for each object to keep track of the position in the world */
public class Position {

    private double coordX;
    private double coordY;

    public Position(double x, double y) {
        coordX = x;
        coordY = y;
    }

    public double getCoordX() {
        return coordX;
    }

    public double getCoordY() {
        return coordY;
    }

    public boolean updateCoordX(double delta){
        // TODO CHECK DOUBLE OVERFLOW
        coordX = coordX + delta;
        return true;
    }
    public boolean updateCoordY(double delta){
        // TODO Same check
        coordY = coordY + delta;
        return true;
    }


}

package places;

import transport.Car;

public class Position {

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

    private double coordX;
    private double coordY;


}

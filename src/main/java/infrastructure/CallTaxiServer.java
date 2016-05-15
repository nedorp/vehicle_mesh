package infrastructure;

import people.Passenger;
import places.Position;
import transport.Car;

import java.util.LinkedList;

import static protocol.CarCommandsCode.*;
import static utility.MathOperation.distance;

/**
 * This is an instance of a CallTaxiServer.
 * Passengers send requests to it and it choose the closer not full car to satisfy his request
 */
public class CallTaxiServer {

    String companyName;
    LinkedList<Car> registeredCars;

    /**
     * This method must be called by an HTTP Server that receives Passenger requests
     */
    public void handleRequest( Passenger passenger, Position callingPosition, Position requiredDestination ){

        // TODO checkPassenger based on registration and past

        Car closestCar = registeredCars.get(0);
        double shortestDistance = distance(closestCar.position, callingPosition);

        // 1st solution: Find the closest Car (or the closest cars) and choose according to the destination
        for(Car currentCar: registeredCars){

            double distanceToPassenger = distance(currentCar.position,callingPosition);

            // The closest car to the passenger
            if(distanceToPassenger < shortestDistance) {
                shortestDistance = distanceToPassenger;
                closestCar = currentCar;
            }
        }

        // 2nd solution
        // todo si potrebbe fare un sort secondo la distanza dal passeggero e in quel caso si potrebbe fare una scelta
        // todo piu accurata. Basata sia sulla vicinanza col passeggero, sia sulla percorrenza

        try {

            closestCar.addCommand(GO_TO, callingPosition);
            closestCar.addCommand(LOAD_PASSENGER, passenger);

        } catch (InterruptedException ie){
            System.out.println("Command Queue interrupted while waiting!");
        }

    }
}

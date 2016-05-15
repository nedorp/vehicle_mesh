/** AUTHOR: Lorenzo Rossi */
package simulation;

import model.MovingObject;
import people.Passenger;
import places.Position;
import places.Station;
import protocol.Talk;
import transport.Car;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

/** Centralized Boss Module that organize all the events
 *  SIMULATES:
 *
 *      wireless connection among cars,
 *      passenger calls to the server,
 *      mechanical movement of object
 *
 *      */
public class StreetGrid {

    // Topology
    private static int GEO_WIDTH = 10000;
    private static int GEO_HEIGHT = 10000;

    // RNG
    private static ThreadLocalRandom rng = ThreadLocalRandom.current();

    private static final long INTERVAL_BEACON = 3000;
    AtomicBoolean freeze = new AtomicBoolean(false);

    final int NUM_THREADS = 10;

    ExecutorService workEngine = Executors.newFixedThreadPool(NUM_THREADS);

    ArrayList<Station> worldStation = new ArrayList<>();
    ArrayList<Car> worldCars = new ArrayList<>();
    ArrayList<Passenger> outsidePassengers = new ArrayList<>();

    public StreetGrid(int numberOfStation, int numberOfCars, int numberOfPassengers){

        int coordX, coordY;

        // Each station has at least one car
        numberOfCars -= numberOfStation;

        // Create Stations
        for(int indexStation = 0; indexStation < numberOfStation; indexStation++){

            // Randomize coordinates
            coordX = rng.nextInt(GEO_WIDTH);
            coordY = rng.nextInt(GEO_HEIGHT);

            Position stationPosition = new Position(coordX,coordY);

            int maxCarsPerStation = (numberOfCars / numberOfStation) * 2;
            // Randomize number of cars for station
            int carsHere = rng.nextInt(numberOfCars);

            // Check for unbalancing station
            if(carsHere > maxCarsPerStation)
                carsHere = maxCarsPerStation;

            numberOfCars -= carsHere;

            Station station = new Station(carsHere + 1, this, stationPosition, "Car_Station_" + (indexStation + 1));
            worldStation.add(station);
            System.out.println("Creating Station in \t\t\t(X: " + coordX + ", Y: " + coordY + ")");


        }

        // Create Passengers
        for(int indexPassenger = 0; indexPassenger < numberOfPassengers; indexPassenger++){

            // Randomize coordinates
            coordX = rng.nextInt(GEO_WIDTH);
            coordY = rng.nextInt(GEO_HEIGHT);

            Position passengerPosition = new Position(coordX,coordY);
            outsidePassengers.add(new Passenger(this,passengerPosition));
        }
    }

    public void worldCycle() throws InterruptedException {

        while (true) {

            freeze.set(true);

            // ====================================================
            // PASSENGER REQUIRE SERVICE
            // ====================================================


            // ====================================================
            // CARS COMMUNICATION
            // ====================================================
            for (int i = 0; i < worldCars.size(); i++) {

                Car transmitterCar = worldCars.get(i);

                // Cars try to communicate
                for (int j = i; j < worldCars.size(); j++) {

                    Car receiverCar = worldCars.get(j);

                    // If someone can listen
                    if (receiverCar.canListenTo(transmitterCar)) {

                        // Creates a talk and let them to interact
                        workEngine.execute(new Talk(transmitterCar, receiverCar));

                    }
                }
            }

            /** Simple case: all the cars are synchronized */
            Thread.sleep(INTERVAL_BEACON);
        }


    }

    /**
     * Allows object to become really movable
     * @param movingObject the object that wants to be moved
     * @param movementInterval time between 2 movements
     */
    public void registerObject(MovingObject movingObject, int movementInterval) {

    }
}
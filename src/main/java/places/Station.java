package places;

import transport.Car;

import java.util.LinkedList;

/** This is a station for cars */
public class Station {

    LinkedList<Car> carPool;

    public Station(int initialCars){

        carPool = new LinkedList<>();

        // Add cars to the carPool
        for(int i=0; i< initialCars;i++){
            carPool.add(new Car(4));
        }
    }

}

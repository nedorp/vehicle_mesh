package places;

import model.StaticObject;
import simulation.StreetGrid;
import transport.Car;

import java.util.LinkedList;

/** This is a station for cars */
public class Station extends StaticObject {

    private static int DEFAULT_SEATS = 4;
    LinkedList<Car> carPool;
    private String name;

    public Station(int initialCars, StreetGrid worldOfAction, Position position, String name){

        // Init static object
        super(position);

        this.name = name;
        carPool = new LinkedList<>();

        // Add cars to the carPool
        System.out.println("Added " + initialCars + " cars in " + name);
        for(int i=0; i< initialCars;i++){
            // TODO Posizionare auto in modo corretto (non sovrapporere) (posizionare in modo equidistante intorno alla stazione)
            // TODO 360° / numcars angolo tra un' auto e l'altra
            carPool.add(new Car(DEFAULT_SEATS,worldOfAction,position));
        }
    }

    public String getName() {
        return name;
    }
}

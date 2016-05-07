package transport;

import model.MovingObject;
import people.Passenger;
import places.Position;
import protocol.CarCommand;
import protocol.CarState;
import utility.MathOperation;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;


// todo si potrebbe porre sia Car che Passenger "extends Position"

public class Car extends MovingObject {

    private static int SEATS = 5;
    private final double RADIUS_FOR_CAR_COMMUNICATION = 10;

    private Passenger[] passengersOnTheCar;

    // This is the on/off condition of the car
    private boolean powerOn;

    // Here people that can pass to another car in a possible Meeting-on-the-way has TRUE value
    // If the passenger allows the change of the car it may get sooner to his/her destination
    private Boolean[] outliers;
    private int nextPassenger = 0;

    // The state of the car
    private CarState carState;

    //============================================
    // COMMAND LIST
    //============================================

    LinkedBlockingQueue<CarCommand> commandsList = new LinkedBlockingQueue<>();


    public Car( int seats){

        passengersOnTheCar = new Passenger[seats];
        outliers = new Boolean[seats];
        carState = CarState.EMPTY;

    }

    public void loadPassenger(Passenger p, boolean lock){

        carState = CarState.FREE_SEATS;

        passengersOnTheCar[nextPassenger] = p;
        if(lock)
            // The passenger doesn't want to change car
            outliers[nextPassenger] = false;

        nextPassenger++;



        if(nextPassenger == SEATS) {

            carState = CarState.REQUIRING_SUPPORT;

            boolean no_outliers = true;
            for(int i=0; i<SEATS; i++){
                if(outliers[i]) {
                    no_outliers = false;
                    break;
                }

            }
            if(no_outliers)
                carState = CarState.FULL;

        }

    }

    /** Check if the transmitter car can be listened by the current car */
    public boolean canListenTo(Car transmitterCar) {
        return MathOperation.distance(position, transmitterCar.position) <= RADIUS_FOR_CAR_COMMUNICATION;
    }

    public void execCommand(CarCommand command) {

        switch (command.getCode()){

            case GO_TO:

                Position destination = (Position) command.getArgument();
                imposeTrajectory(destination);

                break;

            case LOAD_PASSENGER:

                Passenger passenger = (Passenger) command.getArgument();
                boolean locked = true; // todo chiedi all'utente se vuole o no cambiare durante il percorso per arrivare prima!
                loadPassenger(passenger, locked);

        }
    }

    // This cycle must be executed by a separated Thread
    public void init() throws InterruptedException {

        powerOn = true;

        CarCommand currentCommand;

        // Car Core Cycle
        while(powerOn){

            // Blocks if no commands, until a new command arrives
            currentCommand = commandsList.take();

            // Execute the command
            execCommand(currentCommand);

        }
    }

    // This other cycle must be executed by another separated Thread
    public void move(){

    // todo e fixme: SCEGLIERE: è la street grid a muovere le auto o sono le auto a muoversi da sole e la street grid si occupa solo di gestire
        // la comunicazione

    }
}

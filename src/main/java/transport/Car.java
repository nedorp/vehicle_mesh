package transport;

import model.MovingObject;
import people.Passenger;
import places.Position;
import protocol.CarCommand;
import protocol.CarCommandsCode;
import protocol.CarState;
import simulation.StreetGrid;
import utility.MathOperation;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;


// todo si potrebbe porre sia Car che Passenger "extends Position"

public class Car extends MovingObject {

    private static int SEATS = 5;
    private final double RADIUS_FOR_CAR_COMMUNICATION = 10;

    private final static int MOVEMENT_INTERVAL = 500;

    private Passenger[] passengersOnTheCar;

    // This is the on/off condition of the car
    private AtomicBoolean powerOn = new AtomicBoolean();

    // Here people that can pass to another car in a possible Meeting-on-the-way has TRUE value
    // If the passenger allows the change of the car it may get sooner to his/her destination
    private Boolean[] outliersAndNotLocked;
    private int nextPassenger = 0;

    // The state of the car
    private CarState carState;

    //============================================
    // COMMAND LIST
    //============================================

    LinkedBlockingQueue<CarCommand> commandsList = new LinkedBlockingQueue<>();


    /**
     * Create a car under the supervision of the module mover
     * @param seats number of seats for passenger
     * @param mover the module caring about moving the car
     */
    public Car(int seats, StreetGrid mover, Position carPosition){

        // Register the moving object
        super(MOVEMENT_INTERVAL, mover, carPosition);

        passengersOnTheCar = new Passenger[seats];
        outliersAndNotLocked = new Boolean[seats];
        carState = CarState.EMPTY;

    }

    /**
     * Load the passenger p
     * @param p the passenger
     * @param lock is locked if the passenger doesn't want to use the car hopping
     */
    public void loadPassenger(Passenger p, boolean lock){

        carState = CarState.FREE_SEATS;

        passengersOnTheCar[nextPassenger] = p;
        if(lock)
            // The passenger doesn't want to change car
            outliersAndNotLocked[nextPassenger] = false;

        nextPassenger++;

        /* Check if the car is full */
        if(nextPassenger == SEATS) {

            carState = CarState.REQUIRING_SUPPORT_FOR_HOPPING;

            boolean no_outliers = true;
            for(int i=0; i<SEATS; i++){
                if(outliersAndNotLocked[i]) {
                    no_outliers = false;
                    break;
                }

            }
            if(no_outliers)
                carState = CarState.FULL;

        }

    }

    /** Check if the transmitter car can be listened by the current car
     * @param transmitterCar the car that sends beacon*/
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



    // This other cycle must be executed by another separated Thread
    public void move(){

    // todo e fixme: SCEGLIERE: è la street grid a muovere le auto o sono le auto a muoversi da sole e la street grid si occupa solo di gestire
        // la comunicazione

    }

    // ==============================
    // COMMAND SYSTEM
    // ==============================

    // This cycle must be executed by a separated Thread
    public void init() throws InterruptedException {

        powerOn.set(true);

        CarCommand currentCommand;

        // Car Core Cycle
        while(powerOn.get()){

            // Blocks if no commands, until a new command arrives
            currentCommand = commandsList.take();

            // Execute the command
            execCommand(currentCommand);

        }
    }

    public void addCommand(CarCommandsCode commandCode, Object argument) throws InterruptedException {
        CarCommand command = new CarCommand(commandCode,argument);
        commandsList.put(command);
    }
}

import people.Passenger;
import protocol.Talk;
import transport.Car;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class StreetGrid {

    private static final long INTERVAL_BEACON = 3000;
    AtomicBoolean freeze = new AtomicBoolean(false);

    final int NUM_THREADS = 10;

    ExecutorService workEngine = Executors.newFixedThreadPool(NUM_THREADS);

    ArrayList<Car> worldCars = new ArrayList<>();
    ArrayList<Passenger> outsidePassengers = new ArrayList<>();

    public void worldCycle() throws InterruptedException {

        while(true) {

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

            Thread.sleep(INTERVAL_BEACON);
        }


    }


}
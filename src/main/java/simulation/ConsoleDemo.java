package simulation;

import people.Passenger;
import places.Position;
import transport.Car;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import static utility.MathOperation.distance;

/** A small, single-threaded trip simulation using the existing world and entities. */
public class ConsoleDemo {

    private static final double DISTANCE_PER_TICK = 600;
    private static final int ENERGY_PER_STEP = 1;
    private final StreetGrid world;
    private final LinkedList<Request> waiting = new LinkedList<>();
    private final Map<Car, Request> active = new LinkedHashMap<>();
    private final int totalPassengers;
    private int completed;
    private long totalWaitTicks;
    private int pickedUp;
    private double totalDistance;
    private double emptyDistance;
    private long movementSteps;
    private long emptySteps;
    private boolean started;

    public ConsoleDemo(StreetGrid world, long seed) {
        this.world = world;
        Random destinations = new Random(seed);
        int id = 1;
        for (Passenger passenger : world.outsidePassengers) {
            waiting.add(new Request(id++, passenger,
                    new Position(destinations.nextInt(10000), destinations.nextInt(10000))));
        }
        totalPassengers = waiting.size();
    }

    /** Returns completed trips. The tick limit also bounds partially completed scenarios. */
    public int run(int maxTicks, long delayMs) throws InterruptedException {
        if (maxTicks <= 0 || delayMs < 0) {
            throw new IllegalArgumentException("Use a positive tick limit and a nonnegative delay.");
        }
        if (started) {
            throw new IllegalStateException("Create a new demo for each run.");
        }
        started = true;
        System.out.printf(Locale.ROOT,
                "Demo: %d cars, %d passengers, nearest idle car, %.0f units/tick.%n",
                world.worldCars.size(), totalPassengers, DISTANCE_PER_TICK);
        for (Request request : waiting) {
            System.out.printf(Locale.ROOT, "REQUEST P%02d: (%.0f, %.0f) -> (%.0f, %.0f)%n",
                    request.id, request.passenger.position.getCoordX(), request.passenger.position.getCoordY(),
                    request.destination.getCoordX(), request.destination.getCoordY());
        }

        int tick = 0;
        while (completed < totalPassengers && tick < maxTicks) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Demo interrupted");
            }
            tick++;
            assignWaiting(tick);
            // Snapshot entries because completing a trip releases its car.
            for (Map.Entry<Car, Request> entry : new ArrayList<>(active.entrySet())) {
                Car car = entry.getKey();
                Request request = entry.getValue();
                Position target = request.onboard ? request.destination : request.passenger.position;
                double remaining = distance(car.position, target);
                double step = Math.min(DISTANCE_PER_TICK, remaining);
                if (remaining > 0) {
                    car.position.updateCoordX((target.getCoordX() - car.position.getCoordX()) * step / remaining);
                    car.position.updateCoordY((target.getCoordY() - car.position.getCoordY()) * step / remaining);
                    // Each moving car spends one energy unit, including a shorter final step.
                    movementSteps++;
                    if (!request.onboard) emptySteps++;
                }
                totalDistance += step;
                if (request.onboard) {
                    request.passenger.position.updateCoordX(car.position.getCoordX() - request.passenger.position.getCoordX());
                    request.passenger.position.updateCoordY(car.position.getCoordY() - request.passenger.position.getCoordY());
                } else {
                    emptyDistance += step;
                }

                if (remaining <= DISTANCE_PER_TICK) {
                    if (request.onboard) {
                        completed++;
                        active.remove(car);
                        System.out.printf(Locale.ROOT, "[tick %03d] ARRIVAL P%02d with Car%02d at (%.0f, %.0f)%n",
                                tick, request.id, carId(car), car.position.getCoordX(), car.position.getCoordY());
                    } else {
                        request.onboard = true;
                        pickedUp++;
                        totalWaitTicks += tick;
                        System.out.printf(Locale.ROOT, "[tick %03d] PICKUP P%02d by Car%02d, waited %d ticks%n",
                                tick, request.id, carId(car), tick);
                    }
                }
                if (tick % 5 == 0 && active.containsKey(car)) {
                    System.out.printf(Locale.ROOT, "[tick %03d] MOVE Car%02d (%.0f, %.0f), P%02d %s%n",
                            tick, carId(car), car.position.getCoordX(), car.position.getCoordY(), request.id,
                            request.onboard ? "onboard" : "awaiting pickup");
                }
            }
            if (tick % 5 == 0 || completed == totalPassengers) {
                System.out.printf(Locale.ROOT, "[tick %03d] STATUS queued=%d active=%d completed=%d/%d%n",
                        tick, waiting.size(), active.size(), completed, totalPassengers);
            }
            if (delayMs > 0 && completed < totalPassengers && tick < maxTicks) {
                Thread.sleep(delayMs);
            }
        }
        System.out.printf(Locale.ROOT,
                "%nSUMMARY: completed=%d/%d, queued=%d, active=%d, ticks=%d%s%n",
                completed, totalPassengers, waiting.size(), active.size(), tick,
                completed == totalPassengers ? " (all trips completed)" : " (tick limit reached)");
        System.out.printf(Locale.ROOT,
                "Fleet distance: %.1f units; empty: %.1f; with passengers: %.1f%n",
                totalDistance, emptyDistance, totalDistance - emptyDistance);
        System.out.printf(Locale.ROOT,
                "Fleet movement: %d steps; empty: %d; with passengers: %d%n",
                movementSteps, emptySteps, movementSteps - emptySteps);
        System.out.printf(Locale.ROOT, "Fleet energy: %d energy units (%d per moving-car step)%n",
                movementSteps * ENERGY_PER_STEP, ENERGY_PER_STEP);
        System.out.printf(Locale.ROOT, "Average wait until pickup: %.1f ticks (%d picked up)%n",
                pickedUp == 0 ? 0 : (double) totalWaitTicks / pickedUp, pickedUp);
        return completed;
    }

    private void assignWaiting(int tick) {
        while (!waiting.isEmpty()) {
            Request request = waiting.peek();
            Car closest = null;
            double bestDistance = Double.POSITIVE_INFINITY;
            for (Car car : world.worldCars) {
                double pickupDistance = distance(car.position, request.passenger.position);
                if (!active.containsKey(car) && pickupDistance < bestDistance) {
                    closest = car;
                    bestDistance = pickupDistance;
                }
            }
            if (closest == null) {
                return;
            }
            waiting.remove();
            active.put(closest, request);
            System.out.printf(Locale.ROOT, "[tick %03d] ASSIGN P%02d -> Car%02d (pickup distance %.0f)%n",
                    tick, request.id, carId(closest), bestDistance);
        }
    }

    private int carId(Car car) {
        return world.worldCars.indexOf(car) + 1;
    }

    private static class Request {
        final int id;
        final Passenger passenger;
        final Position destination;
        boolean onboard;

        Request(int id, Passenger passenger, Position destination) {
            this.id = id;
            this.passenger = passenger;
            this.destination = destination;
        }
    }
}

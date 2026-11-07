package simulation;

import places.Position;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Dependency-free behavioral checks; run with java -cp target/classes simulation.ConsoleDemoTest. */
public class ConsoleDemoTest {

    public static void main(String[] args) throws Exception {
        StreetGrid world = new StreetGrid(3, 6, 12, 42);
        check(world.worldCars.size() == 6, "The entire fleet must be registered");
        check(world.worldCars.get(0).position != world.worldCars.get(1).position,
                "Cars at the same station must not share a mutable position");
        String first = run(world, 43, 200, 12);
        Random destinations = new Random(43);
        for (int i = 0; i < 12; i++) {
            check(close(world.outsidePassengers.get(i).position.getCoordX(), destinations.nextInt(10000))
                            && close(world.outsidePassengers.get(i).position.getCoordY(), destinations.nextInt(10000)),
                    "Every passenger must reach their own destination");
        }
        check(first.equals(run(new StreetGrid(3, 6, 12, 42), 43, 200, 12)),
                "The same seed must reproduce assignments, movements, and metrics");

        // Two nearby passengers must get distinct nearest cars and both board in one tick.
        StreetGrid pickup = new StreetGrid(1, 2, 2, 1);
        set(pickup.worldCars.get(0).position, 0, 0);
        set(pickup.worldCars.get(1).position, 9000, 9000);
        set(pickup.outsidePassengers.get(0).position, 600, 0);
        set(pickup.outsidePassengers.get(1).position, 9000, 8400);
        String partial = run(pickup, 43, 1, 0);
        check(partial.contains("ASSIGN P01 -> Car01") && partial.contains("ASSIGN P02 -> Car02"),
                "Each request must use its nearest idle car");
        check(partial.contains("PICKUP P01") && partial.contains("PICKUP P02")
                        && partial.contains("tick limit reached"), "The tick limit must preserve unfinished trips");
        check(close(pickup.worldCars.get(0).position.getCoordX(), 600)
                        && close(pickup.worldCars.get(1).position.getCoordY(), 8400),
                "Cars must move to pickup without overshooting");
        check(partial.contains("Fleet movement: 2 steps; empty: 2; with passengers: 0")
                        && partial.contains("Fleet energy: 2 energy units"),
                "Two cars moving during one world tick must consume two energy units");

        // Short final movements still cost one unit; the idle second car costs nothing.
        StreetGrid energy = new StreetGrid(1, 2, 1, 1);
        set(energy.worldCars.get(0).position, 8726, 5051);
        set(energy.worldCars.get(1).position, 0, 0);
        set(energy.outsidePassengers.get(0).position, 8736, 5051);
        String shortTrip = run(energy, 43, 2, 1);
        check(shortTrip.contains("Fleet distance: 30.0")
                        && shortTrip.contains("Fleet movement: 2 steps; empty: 1; with passengers: 1")
                        && shortTrip.contains("Fleet energy: 2 energy units"),
                "Energy must count actual car movements, including empty and shorter final steps, but not idle cars");

        // Fleet metrics must account separately for approach and passenger travel.
        StreetGrid single = new StreetGrid(1, 1, 1, 1);
        set(single.worldCars.get(0).position, 0, 0);
        set(single.outsidePassengers.get(0).position, 600, 0);
        String trip = run(single, 43, 200, 1);
        Matcher metrics = Pattern.compile("Fleet distance: ([0-9.]+) units; empty: ([0-9.]+); with passengers: ([0-9.]+)").matcher(trip);
        check(metrics.find(), "Distance metrics must be printed");
        double occupied = Math.hypot(8756 - 600, 5051);
        check(Math.abs(Double.parseDouble(metrics.group(1)) - (600 + occupied)) < 0.06
                        && close(Double.parseDouble(metrics.group(2)), 600)
                        && Math.abs(Double.parseDouble(metrics.group(3)) - occupied) < 0.06,
                "Distances must reflect actual travel, including the final partial step");

        // Starting at both pickup and destination must not introduce NaN or a stuck trip.
        StreetGrid zeroDistance = new StreetGrid(1, 1, 1, 1);
        set(zeroDistance.worldCars.get(0).position, 8756, 5051);
        set(zeroDistance.outsidePassengers.get(0).position, 8756, 5051);
        String stationary = run(zeroDistance, 43, 2, 1);
        check(stationary.contains("Fleet distance: 0.0") && !stationary.contains("NaN"),
                "Zero-distance trips must complete without invalid coordinates");
        check(stationary.contains("Fleet movement: 0 steps") && stationary.contains("Fleet energy: 0 energy units"),
                "A zero-distance trip must consume no movement energy");
        String emptyScenario = run(new StreetGrid(1, 1, 0, 1), 43, 1, 0);
        check(emptyScenario.contains("all trips completed") && emptyScenario.contains("Fleet energy: 0 energy units"),
                "An empty scenario must finish immediately");
        try {
            new StreetGrid(2, 1, 0);
            throw new AssertionError("Invalid fleet allocation must be rejected");
        } catch (IllegalArgumentException expected) {
            // Expected: each station requires a car.
        }
        System.out.println("PASS: completion, reproducibility, nearest-car dispatch, movement, distance/energy metrics, and edge cases.");
    }

    private static String run(StreetGrid world, long seed, int ticks, int expectedCompleted) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream original = System.out;
        PrintStream captured = new PrintStream(buffer, true, "UTF-8");
        try {
            System.setOut(captured);
            check(new ConsoleDemo(world, seed).run(ticks, 0) == expectedCompleted, "Unexpected completed trip count");
        } finally {
            System.setOut(original);
            captured.close();
        }
        return buffer.toString("UTF-8");
    }

    private static void set(Position position, double x, double y) {
        position.updateCoordX(x - position.getCoordX());
        position.updateCoordY(y - position.getCoordY());
    }

    private static boolean close(double actual, double expected) {
        return Math.abs(actual - expected) < 0.000001;
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}

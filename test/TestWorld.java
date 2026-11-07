import simulation.StreetGrid;
import simulation.ConsoleDemo;

/** Test */
public class TestWorld {

    public static void main(String[] args) throws InterruptedException {

        long delayMs = 150;
        long seed = 42;
        try {
            if (args.length > 2) {
                throw new IllegalArgumentException();
            }
            if (args.length > 0) delayMs = Long.parseLong(args[0]);
            if (args.length > 1) seed = Long.parseLong(args[1]);
            if (delayMs < 0) throw new IllegalArgumentException();
        } catch (IllegalArgumentException exception) {
            System.err.println("Usage: java -cp target/classes TestWorld [delayMs >= 0] [seed]");
            System.exit(1);
            return;
        }

        System.out.println("Vehicle_mesh 0.1");
        System.out.println(" ___      ___ _______   ___  ___  ___  ________  ___       _______           _____ ______   _______   ________  ___  ___          ________                    _____     \n" +
                "|\\  \\    /  /|\\  ___ \\ |\\  \\|\\  \\|\\  \\|\\   ____\\|\\  \\     |\\  ___ \\         |\\   _ \\  _   \\|\\  ___ \\ |\\   ____\\|\\  \\|\\  \\        |\\   __  \\                  / __  \\    \n" +
                "\\ \\  \\  /  / | \\   __/|\\ \\  \\\\\\  \\ \\  \\ \\  \\___|\\ \\  \\    \\ \\   __/|        \\ \\  \\\\\\__\\ \\  \\ \\   __/|\\ \\  \\___|\\ \\  \\\\\\  \\       \\ \\  \\|\\  \\                |\\/_|\\  \\   \n" +
                " \\ \\  \\/  / / \\ \\  \\_|/_\\ \\   __  \\ \\  \\ \\  \\    \\ \\  \\    \\ \\  \\_|/__       \\ \\  \\\\|__| \\  \\ \\  \\_|/_\\ \\_____  \\ \\   __  \\       \\ \\  \\\\\\  \\               \\|/ \\ \\  \\  \n" +
                "  \\ \\    / /   \\ \\  \\_|\\ \\ \\  \\ \\  \\ \\  \\ \\  \\____\\ \\  \\____\\ \\  \\_|\\ \\       \\ \\  \\    \\ \\  \\ \\  \\_|\\ \\|____|\\  \\ \\  \\ \\  \\       \\ \\  \\\\\\  \\      ___          \\ \\  \\ \n" +
                "   \\ \\__/ /     \\ \\_______\\ \\__\\ \\__\\ \\__\\ \\_______\\ \\_______\\ \\_______\\       \\ \\__\\    \\ \\__\\ \\_______\\____\\_\\  \\ \\__\\ \\__\\       \\ \\_______\\    |\\__\\          \\ \\__\\\n" +
                "    \\|__|/       \\|_______|\\|__|\\|__|\\|__|\\|_______|\\|_______|\\|_______|        \\|__|     \\|__|\\|_______|\\_________\\|__|\\|__|        \\|_______|    \\|__|           \\|__|\n" +
                "                                                                                                        \\|_________|                                                    \n");
        System.out.println("Creating the world...");
        System.out.println("Scenario seed: " + seed);
        StreetGrid world = new StreetGrid(3, 6, 12, seed);
        new ConsoleDemo(world, seed + 1).run(200, delayMs);

    }
}

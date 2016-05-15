package implementation;

import model.MovementEngine;
import places.Position;

import java.util.concurrent.ThreadLocalRandom;

/**
 * This implements the Linear Congruential Generator. Not used.
 */
public class LCGMovement implements MovementEngine {

    // Movement auxiliary fields
    private int turnsInPause = 0;
    private int turnsInMovement = 0;
    private double moveOnX = 0;
    private double moveOnY = 0;

    // Constants
    private static final int INT_FOR_PAUSE = 8;
    private static final int MAX_TURNS_IN_MOVEMENT = 90;
    private static final int MAX_TURNS_IN_PAUSE = 11;
    private static final double MAX_DELTA = 10;

    // RNG
    private static final ThreadLocalRandom randNumGen = ThreadLocalRandom.current();

    /** Move the position, modifies the object Position? */
    @Override
    public void move(Position objectPosition, double speed) {

        // Check pause
        if(turnsInPause > 0){
            turnsInPause--;
            return;
        }

        // Check not-terminated movement
        if(turnsInMovement > 0){
            turnsInMovement--;
            return;
        }

        // Extract for pause or movement
        if(ThreadLocalRandom.current().nextInt(10) > INT_FOR_PAUSE){
            // PAUSE
            turnsInPause = randNumGen.nextInt(MAX_TURNS_IN_PAUSE);
            return;
        }
        else {
            // MOVE
            turnsInMovement = randNumGen.nextInt(MAX_TURNS_IN_MOVEMENT);
            moveOnX = randNumGen.nextDouble(MAX_DELTA);
            moveOnY = randNumGen.nextDouble(MAX_DELTA);

            // Update Position
            objectPosition.updateCoordX(moveOnX);
            objectPosition.updateCoordY(moveOnY);
            return;

        }
    }
}

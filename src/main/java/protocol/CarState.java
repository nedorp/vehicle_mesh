package protocol;


public enum CarState {

    FULL,                       /** All seats are occupied, trajectory is good, no interaction (only ping) */
    EMPTY,                      /** All seats are free. transport.Car is stopped */
    FREE_SEATS,                 /** Available seats for other cars */
    REQUIRING_SUPPORT_FOR_HOPPING           /** The car has some outliers. It needs to pass them to some other cars met on the way */

}

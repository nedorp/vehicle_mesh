package protocol;

/**
 * Auxiliary class
 */
public class CarCommand {

    public CarCommandsCode getCode() {
        return code;
    }

    public Object getArgument() {
        return argument;
    }

    CarCommandsCode code;
    Object argument;

    public CarCommand(CarCommandsCode commandCode, Object argument) {
        code = commandCode;
        this.argument = argument;
    }
}

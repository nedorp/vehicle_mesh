package utility;

import places.Position;

public class MathOperation {

    public static double distance(Position p, Position q){
        return Math.sqrt(

                Math.pow(
                        p.getCoordX() - q.getCoordX()
                        ,2
                ) +
                Math.pow(
                        p.getCoordY() - q.getCoordY()
                        ,2
                )
        );
    }

    public static void main(String[] args) {
        System.out.println(distance(new Position(8.8,9.9),new Position(9.9,8.8)));
    }
}

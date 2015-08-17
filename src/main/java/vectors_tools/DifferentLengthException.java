package vectors_tools;

/**
 * Created by Ron on 08/08/2015.
 */
public class DifferentLengthException extends VectorException {

    public static long serialVersionUID = 1L;

    DifferentLengthException() {
        super("Distance can't be calculated between vectors not in the same dimensions");
    }


}

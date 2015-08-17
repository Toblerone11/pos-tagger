package vectors_tools;

/**
 * Created by Ron on 08/08/2015.
 */
public class SmoothExcption extends VectorException {
    public static long serialVersionUID = 1L;

    public SmoothExcption() {
        super("encountered division by zero: Failed To smooth");
    }
}

package vectors_tools;

/**
 * when failed to smooth
 * Created by Ron on 08/08/2015.
 */
public class SmoothException extends VectorException {
    public static long serialVersionUID = 1L;

    public SmoothException() {
        super("encountered division by zero: Failed To smooth");
    }
}

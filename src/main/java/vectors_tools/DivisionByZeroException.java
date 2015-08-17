package vectors_tools;

/**
 * Created by Ron on 08/08/2015.
 */
public class DivisionByZeroException extends ArithmeticException {
    public static long serialVersionUID = 1L;

    public DivisionByZeroException() {
        super("encountered division by zero");
    }
}

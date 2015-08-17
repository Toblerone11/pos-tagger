package corpusdata;

/**
 * Created by Ron
 */
public class WordIsNotExsistException extends Exception {
    public static long serialVersionUID = 1L;

    public WordIsNotExsistException(int wordRepr) {
        super("the given index doesn't represent any word: " + wordRepr);
    }
}

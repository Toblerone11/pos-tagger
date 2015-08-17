package vectors_tools;

/**
 * Created by Ron on 10/08/2015.
 */
public class VectorOperators {

    /**
     * add to vectors
     * @param vector1
     * @param vector2
     * @return the sum between two vectors
     */
    public static int[] addVectors(int[] vector1, int[] vector2) {
        if (vector1.length != vector2.length)
            return null;
        for (int i = 0; i < vector1.length; i++) {
            vector1[i] += vector2[i];
        }
        return vector1;
    }

    /**
     * get vector of zeros only
     * @param length the length of the desird vector
     * @return vector of zeros
     */
    public static int[] getZerosVector(int length) {
        return new int[length];
    }
}

package vectors_tools;

import corpusdata.Tagable;

/**
 * a class which implements the Kullback-Leibler Divergence function.
 * this function defines asymetric vector space.
 * Created by Ron
 */
public class KLD {


    static double[] normalizeVector(int[] vector, int totalCount) {

        double[] result = new double[vector.length];
        for (int idx = 0; idx < vector.length; idx++)
            result[idx] = (double) vector[idx] /(double) totalCount;

        return result;
    }

    public static double calculateDistance(Tagable word1, Tagable word2) {

            int[] vector1 = word1.getClusterContextDistribution();
            int[] vector2 = word2.getClusterContextDistribution();
            try {
                if (vector1.length != vector2.length)
                    throw new DifferentLengthException();
            } catch (DifferentLengthException e) {
                e.printStackTrace();
            }

        int totalCountWord1 = word1.getFrequency();
            double[] normalVector1 = normalizeVector(vector1, word1.getFrequency());
            double[] normalVector2 = normalizeVector(vector2, word2.getFrequency());

            double result = 0;
            for (int idx = 0; idx < vector1.length; idx++)
                try {
                    if (normalVector1[idx] == 0) { //the whole current expression is 0.
                        continue;
                    }
                    result += (normalVector1[idx] * Math.log(normalVector1[idx] / normalVector2[idx]));
                }
                catch (DivisionByZeroException e) {
                    System.err.println(e.getMessage());
                    System.out.println("smoothing again");
                    normalVector2 = Smoothing.smoothProbabilities(word2);
                }


        return result;
    }

    /**
     * calculating the entropy of some distribution.
     * @param p distribution vector.
     * @return the negative value of the entropy.
     */
    public static double calculateEntropy(double[] p) {
        double entropy = 0;
        for (double x : p) {
            if (x == 0) {
                continue;
            }
            entropy += (x * Math.log(x));
        }
        return -entropy;
    }


    public static double calculateDistance2(Tagable word1, Tagable word2)
            throws VectorException {

        int[] vector1 = word1.getClusterContextDistribution();
        int[] vector2 = word2.getClusterContextDistribution();
        if (vector1.length != vector2.length)
            throw new DifferentLengthException();

        int totalCountWord1 = word1.getFrequency();
        double[] normalVector1 = normalizeVector(vector1, word1.getFrequency());
        double[] normalVector2 = normalizeVector(vector2, word2.getFrequency());

        double result = -calculateEntropy(normalVector1); // initializing the result variable with negative entropy.
        boolean notCalculated = true;
        while (notCalculated) {
            notCalculated = false;
            for (int x = 0; x < normalVector2.length; x++) {
                try {
                    if (normalVector2[x] == 0)
                        throw new DivisionByZeroException();
                    result += vector1[x] * (Math.log(normalVector2[x]));
                } catch (ArithmeticException e) { //failed to smooth - correct smoothing.
                    System.err.printf("%s\ntrying to smooth againn\n", e.getMessage());
                    notCalculated = true;
                    normalVector2 = Smoothing.smoothProbabilities(word2);
                    break;
                }
            }
        }
        result = Math.log(result) / totalCountWord1;
        return -result / totalCountWord1;
    }




}

package vectors_tools;

import corpusdata.Cluster;
import corpusdata.Tagable;
import corpusdata.Word;

/**
 * a class which implements the Kullback-Leibler Divergence function.
 * this function defines asymetric vector space.
 * Created by Ron
 */
public class KLD {


    static double[] normalizeVector(int[] vector, int totalCount) {

        double[] result = new double[vector.length];
        for (int idx = 0; idx < vector.length; idx++)
            result[idx] = (double) vector[idx] / (double) totalCount;

        return result;
    }

    static double[] normalizeVector(double[] vector, int totalCount) {

        double[] result = new double[vector.length];
        for (int idx = 0; idx < vector.length; idx++)
            result[idx] = vector[idx] / (double) totalCount;

        return result;
    }

    /**
     * calculating the Kullback-leibler Divergence.
     *
     * @param vector1
     * @param vector2
     * @return
     */
    private static double calculateDistance(double[] vector1, double[] vector2, Cluster cluster2)
            throws SmoothException {

        double result = 0;
        boolean smoothed = true;
        boolean triedOnce = false;

        while (!triedOnce && smoothed) {
            try {
                for (int idx = 0; idx < vector1.length; idx++) {
                    if (vector1[idx] == 0) { //the whole current expression is 0.
                        continue;
                    }
                    result += (vector1[idx] * Math.log(vector1[idx] / vector2[idx]));
                }
                smoothed = false;
            } catch (DivisionByZeroException e) {
                if (!triedOnce) {
                    cluster2.calculateDistribution();
                    vector2 = cluster2.getSmoothedContexts();
                    smoothed = true;
                } else {
                    System.err.println(e.getMessage());
                    System.out.println("smoothing again");
                    throw new SmoothException();
                }
            } finally {
                triedOnce = true;
            }
        }
        return result;
    }

    public static double calculateDistanceClusterCluster(Cluster cluster1, Cluster cluster2) throws SmoothException {
        double[] vector1 = cluster1.getSmoothedContexts();
        double[] vector2 = cluster2.getSmoothedContexts();

        try {
            if (vector1.length != vector2.length)
                throw new DifferentLengthException();
        } catch (DifferentLengthException e) {
            e.printStackTrace();
        }

        double result = calculateDistance(vector1, vector2, cluster2);

        return result;
    }


    public static double calculateDistanceWordCluster(Word word, Cluster cluster) throws SmoothException {

        int[] vector1 = word.getClusterContextDistribution();
        double[] vector2 = cluster.getSmoothedContexts();
        double sumVector2 = 0;

        try {
            if (vector1.length != vector2.length)
                throw new DifferentLengthException();
        } catch (DifferentLengthException e) {
            e.printStackTrace();
        }
        double[] normalVector1 = normalizeVector(vector1, word.getFrequency());

        double result = calculateDistance(normalVector1, vector2, cluster);

        return result;
    }

    /**
     * calculating the entropy of some distribution.
     *
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

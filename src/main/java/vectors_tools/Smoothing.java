package vectors_tools;

import corpusdata.Tagable;

import java.util.*;

/**
 * Created by Ron on 08/08/2015.
 */

public class Smoothing {

    /* constants */
    private static final int COUNT_1 = 1;
    private static final int ZEROS_INDEX = 0;
    private static final int MIN_LENGTH_TO_SMOOTH = 6;

    /* statics */
    private static int[] counts = null;
    private static int[] countOfCounts = null;


    /**
     * sorting counts by value of the count and count number of items with same count
     *
     * @param distribution the vector to count its counts and sort
     */
    private static void sortCounts(int[] distribution) {
        TreeMap<Integer, Integer> countMap = new TreeMap<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer int1, Integer int2) {
                return Integer.compare(int1, int2);
            }
        });

        /*  count all of the counts */
        for (int x : distribution) {
            if (countMap.containsKey(x))
                countMap.put(x, countMap.get(x) + 1);
            else
                countMap.put(x, 1);
        }

        /* count of  */

        /* seperate keys - counts and values - count of counts - convert into two arrays of ints */
        int index = 0;
        counts = new int[countMap.size()];
        countOfCounts = new int[countMap.size()];

        for (Map.Entry<Integer, Integer> entry : countMap.entrySet()) {
//            System.out.println("<" + entry.getKey() + " , " + entry.getValue() + " >");

            counts[index] = entry.getKey();
            countOfCounts[index] = entry.getValue();
            index++;
        }
    }

    /**
     * smooth the vector distrbution of some word to avoid singularities points
     * (equals to zero in our case). using Simple good turing smoothing.
     *
     * @param toTag the part of speech data to tag
     * @return smoothed distribution vector
     */
    public static double[] smoothProbabilities(Tagable toTag) {
        int[] distribution = toTag.getClusterContextDistribution();
        sortCounts(distribution); //counting numbers of occurrences of different types.

        if (!needSmooth(distribution)) { // if there is no zeros in the count.
            return KLD.normalizeVector(distribution, toTag.getFrequency());
        }

        //fix probabilities
        if (counts.length < MIN_LENGTH_TO_SMOOTH) {
            int[] newCounts = new int[MIN_LENGTH_TO_SMOOTH];
            int[] newCountOfCounts = new int[MIN_LENGTH_TO_SMOOTH];
            try {
                fixVectorDistribution(counts, countOfCounts, newCounts, newCountOfCounts);
                counts = newCounts;
                countOfCounts = newCountOfCounts;
            } catch (ArrayIndexOutOfBoundsException e) {
//                System.out.println(toTag.getName() + " in index: " + toTag.index);
                return createDefaultVector(distribution.length);
            }

        }
        /* initial SimpleGoodTuring object without the zeros counting to compute the smoothed probabilities */
        SimpleGoodTuring sgt = new SimpleGoodTuring(
                Arrays.copyOfRange(counts, COUNT_1, counts.length),
                Arrays.copyOfRange(countOfCounts, COUNT_1, countOfCounts.length));

        /* smoothing was needed, thus there is count of zeros */
        int zeroCount = countOfCounts[ZEROS_INDEX];

        /* merge all counts to one HashMap -
        fast way to find count of some type and the smoothed probability related to it */
        double probabilityForSingleUnseen = (sgt.getProbabilityForUnseen() / (double) zeroCount);
        double[] smoothedCounts = sgt.getProbabilities();
        HashMap<Integer, Double> smoothMap = new HashMap<>(smoothedCounts.length);
        smoothMap.put(counts[ZEROS_INDEX], probabilityForSingleUnseen);
        for (int i = 0; i < smoothedCounts.length; i++)
            smoothMap.put(counts[i + 1], smoothedCounts[i]);

        /* prepare result */
        double[] smoothedVector = new double[distribution.length];
        for (int i = 0; i < smoothedVector.length; i++)
            smoothedVector[i] = smoothMap.get(distribution[i]);

        counts = null;
        countOfCounts = null;
        return smoothedVector;
    }

    /**
     * checking if some distribution have singularities points (zeros probabilities).
     * assuming sortCounts has been activated before
     *
     * @param distribution vector of distribution.
     * @return true if need smoothing (there is zero), false otherwise.
     */
    private static boolean needSmooth(int[] distribution) {
        if (counts == null) {
            System.err.println("distribution vector hasn't counted yet");
            sortCounts(distribution);
        }
        if (counts[0] == 0)
            return true;

        return false;
    }

    /**
     * assuming length of the two vectors are less then 5.
     * taking pair of vectors which should be input to the smoothing method
     * and adapting them
     *
     * @param n    vector of counts.
     * @param r    vector of count o counts related to n.
     * @param newN new vector to adapt by the old n.
     * @param newR new vector to adapt by the old r.
     */
    private static void fixVectorDistribution(int[] n, int[] r, int[] newN, int[] newR)
            throws ArrayIndexOutOfBoundsException {
        int lenOfVectors = n.length;

        for (int index = lenOfVectors; index < MIN_LENGTH_TO_SMOOTH; index++) {
            newN[index] = n[index - 1] + 1;
            newR[index] = 1;
        }

        for (int i = 0; i < lenOfVectors; i++) {
            newN[i] = n[i];
            newR[i] = r[i];
        }
    }


    private static double[] createDefaultVector(int size) {
        double[] defaultVector = new double[size];
        double defaultValue = 1.0 / size;
        for (int i = 0; i < size; i++) {
            defaultVector[i] = defaultValue;
        }
        return defaultVector;
    }
}

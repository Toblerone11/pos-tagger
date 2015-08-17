package learner;

/**
 * Created by Ron
 */
public class StaticVariables {

    /* statics */
    private static int NUM_OF_CLUSTERS = 0;
    private static int RARE_WORD_TRESHOLD = 0;

    public static int getNumOfClusters() {
        return NUM_OF_CLUSTERS;
    }

    public static void setNumOfClusters(int numOfClusters) {
        NUM_OF_CLUSTERS = numOfClusters;
    }

    public static int getRareWordTreshold() {
        return RARE_WORD_TRESHOLD;
    }

    public static void setRareWordTreshold(int rareWordTreshold) {
        RARE_WORD_TRESHOLD = rareWordTreshold;
    }
}

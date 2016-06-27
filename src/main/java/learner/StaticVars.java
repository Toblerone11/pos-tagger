package learner;

/**
 * Created by Ron
 */
public class StaticVars {

    /* constant */
    static final String NAME_OF_CONTEXT = "words_context", NAME_OF_DICTIONARY = "words_indices";

    /* statics */
    private static int NUM_OF_CLUSTERS = 0;
    private static int RARE_WORD_TRESHOLD = 0;
    private static double MERGE_CLUSTERS_TRESHOLD = 0;
    private static double WORD_TO_CLUSTER_TRESHOLD = 0;

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

    public static void setMergeTreshold(double mergeTreshold) {
        MERGE_CLUSTERS_TRESHOLD = mergeTreshold;
    }

    public static void setWordToClusterTreshold(double wordToClusterTreshold) {
        WORD_TO_CLUSTER_TRESHOLD = wordToClusterTreshold;
    }

    public static double getMergeTreshold() {
        return MERGE_CLUSTERS_TRESHOLD;
    }

    public static double getClusterTreshold() {
        return WORD_TO_CLUSTER_TRESHOLD;
    }
}

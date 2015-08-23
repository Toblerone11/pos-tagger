package learner;

/**
 * [pathToDir], [numOfClusters], [mergeTreshold], [rareTreshold], [clusterTreshold]
 * "/home/ec2-user/cluster_words/"
 * Created by Ron
 */
public class POSLearner {

    /* statics */
    private static String pathToCorpusDir = "";
    private static String pathToOutputDir = ".\\";


    public static void main(String[] args) {

        pathToCorpusDir = args[0];
        pathToOutputDir = args[1];

        StaticVariables.setNumOfClusters(Integer.parseInt(args[2]));
        StaticVariables.setRareWordTreshold(Integer.parseInt(args[3]));
        StaticVariables.setMergeTreshold(Double.parseDouble(args[4]));
        StaticVariables.setWordToClusterTreshold(Double.parseDouble(args[5]));


        ClusterWords learner = new ClusterWords(pathToCorpusDir, pathToOutputDir);
        learner.learnPOS();
    }
}

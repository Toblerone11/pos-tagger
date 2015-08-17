package learner;

/**
 * [pathToDir], [numOfClusters], [mergeTreshold], [rareTreshold], [clusterTreshold]
 * "/home/ec2-user/cluster_words/"
 * Created by Ron
 */
public class POSLearner {

    public static void main(String[] args) {
        ClusterWords learner = new ClusterWords("D:\\cluster_words\\", 50, 0.1, 20, 2);
        learner.learnPOS();
    }
}

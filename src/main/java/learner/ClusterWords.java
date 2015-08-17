package learner;

import corpusdata.Cluster;
import corpusdata.Vocabulary;
import corpusdata.Word;
import corpusdata.WordIsNotExsistException;
import corpusdata.files.DataGetter;
import vectors_tools.KLD;
import vectors_tools.VectorException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;


/**
 * this class implementing the algorithm of alexander clark:
 * 0. initialization - each cluster gets one of the K most frequent words
 * 1. iterate until converage (or until covering of at least P% percent of the words):
 *    1.0 calculate distribution of clusters (average of all words)
 *    1.1 find close clusteres to merge.
 *    1.2 iterate over words that occurs more than 50 times:
 *        1.2.1 calculate the context distribution.
 *        1.2.2 find the closest cluster and the second closest cluster
 *
 *    1.3 sort the words by the ratio between the closest and between the second closest.
 *    1.4 add the best words (which the ration for them is above some constant) to their closst clusters.
 *
 * 2. write clusters to files.
 * Created by Ron on 10/08/2015.
 */
public class ClusterWords {

    /* constants */
    private static final int FOR_UNCLUSTERED = 1, UNCLUSTERED_INDEX = 0;
    private static final int START_OF_CLUSTERS = 1;
    //private static final int WORD_INDEX = 0, WORD_FREQUENCY = 1;

    /* static */
    static int numOfIterations = 0;

    /* data members */
    DataGetter dg;
    Cluster[] allClusters;
    public int numOfClusters;
    final double MERGE_TRESHOLD;
    final double RARE_TRESHOLD;
    final double CLUSTER_TRESHOLD;
    private int maxWordToCluster;
    private int converageTreshold;
    private String pathToDir;
    private Recorder recorder;

    /* constructor */

    /**
     * C'tor for the minimal parameters.
     * @param pathToDir the directory eith all of the data
     * @param numOfClusters number of clusters.
     */
    public ClusterWords(String pathToDir, int numOfClusters, double mergeTreshold, int rareTreshold, double clusterTreshold) {
        this.pathToDir = pathToDir;
        recorder = new Recorder(pathToDir);
        StaticVariables.setNumOfClusters(numOfClusters);
        StaticVariables.setRareWordTreshold(rareTreshold);
        dg = DataGetter.getInstance(pathToDir, "all_words.txt", "dictionary.txt");
        Vocabulary.instance().restartIterator();
        this.numOfClusters = numOfClusters + FOR_UNCLUSTERED;
        allClusters = new Cluster[this.numOfClusters];
        MERGE_TRESHOLD = mergeTreshold;
        RARE_TRESHOLD = rareTreshold;
        CLUSTER_TRESHOLD = clusterTreshold;

        int vocabSize = Vocabulary.instance().getFrequentWords().size();
        this.converageTreshold = (int) Math.floor((vocabSize * 80.0 / 100)); // 80% of the corpus.
        this.maxWordToCluster = (int) Math.floor(vocabSize * 1.0 / 100); // 1% of the corpus.
    }


    /**
     * run the algorithm described in the readme.
     * learn Part Of Speech!
     */
    public void learnPOS() {
        initializeClusters();
        iterUntilConverage();
        for (Cluster cluster : allClusters) {
            recorder.writeClusterToFile(cluster);
        }
        recorder.writeLogToFile();
    }

    /**
     * initial first clusters with most common words in the corpus
     */
    private void initializeClusters() {
        System.out.print("initializing clusters : ");
        Cluster unclustered = Cluster.initialClusterForUnclustered();
        allClusters[UNCLUSTERED_INDEX] = unclustered;
        for (int clusterIndex = 1; clusterIndex < this.numOfClusters; clusterIndex++) {
            Word toBeClustered = Vocabulary.instance().getMostFrequentUnclusteredWord();
            allClusters[clusterIndex] = new Cluster(toBeClustered); //create new cluster with the next most frequent word.
            System.out.printf("cluster %d initialized with word %s\n", clusterIndex, toBeClustered.getName());
        }

        //the rest of the words initialized to the unclustered category
        assert unclustered != null;
        unclustered.addAllWords(Vocabulary.instance().getAllUnclustererdWords());

        allClusters[0] = unclustered;
        System.out.println("Done");
    }

    /**
     * iterating in order to cluster all words until 80% percent of the words are clustered.
     */
    private void iterUntilConverage() {
        System.out.println("starting iterations");
        int converageMarker = (Word.numOfWords - Word.numOfRareWords) * this.converageTreshold / 10;
        while (Word.numOfClusteredWords < converageMarker) {
            System.out.printf("Iteraration %d\twords left to cluster: %d\n",
                    numOfIterations++, (Word.numOfWords - Word.numOfRareWords) - Word.numOfClusteredWords);

            iterateToCluster();
        }
        System.out.println("---------clustering process finished--------");
    }

    /**
     * starting with update the distribution of all clusters and find close clusters to merge.
     * next, it iterates over all frequent enough words that are still unclustered, in order to find
     * their closest clluster. then all words with best scores are clustered respectively.
     */
    private void iterateToCluster() {
        /* update distributions of all clusters */
        updateClustersDistribution();

        /* check for close clusters step */
        if(mergeCloseClusters())
            updateClustersDistribution(); // distributions update needed.

        /* get all unrare unclustered words, their closest cluster and the distance */
        TreeSet<Double[]> closestClusterToWords =
                findClosestClusterToAllWords(Vocabulary.instance().getFrequentWords());

        /* cluster all words that are close enough to some cluster */
        Iterator<Double[]> iterWords = closestClusterToWords.iterator();
        int numOfClusteredWords = 0;
        clusterLoop:
        while (iterWords.hasNext()) {
            Double[] wordWithCluster = iterWords.next();
            if (numOfClusteredWords > this.maxWordToCluster) {
                if (wordWithCluster[2] > CLUSTER_TRESHOLD) {
                    break clusterLoop;
                }
            }
            int clusterIndex = (int) Math.floor(wordWithCluster[2]);
            int wordIndex = (int) Math.floor(wordWithCluster[0]);
            allClusters[clusterIndex].addWord(wordIndex);
            numOfClusteredWords++;
            try {
                System.out.printf("the word \"%s\" was clustered: cluster \"%s\"\n",
                        Vocabulary.instance().getWordByIndex(wordIndex).getName(),
                        allClusters[clusterIndex].getName());
            } catch (WordIsNotExsistException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Iterate over all existing clusters and calculate their distribution according t all words in them.
     * Ignoring the default cluster for unclustered words.
     */
    private void updateClustersDistribution() {
        for (Cluster cluster : allClusters) {
            if (cluster.index != 0)
                cluster.calculateDistribution();
        }
    }

    /**
     * if some cluser is close enough to another by the KLD, the method merges the close cluster into the other.
     * After that the method create a new cluster instead of the one which had been merged.
     * @param clusterIndex1 the cluster to merge into.
     * @param clusterIndex2 the cluster to merge to other close merge.
     */
    private void mergeClusters(int clusterIndex1, int clusterIndex2) {
        allClusters[clusterIndex1].mergeWithOtherCluster(allClusters[clusterIndex2]);
        allClusters[clusterIndex2].restartCluster(Vocabulary.instance().getMostFrequentUnclusteredWord());
    }

    /**
     * checks K^2 distances between every pair in two directions (non-symetric divergence function).
     * if the divergence between two clusters falls below the given treshold - the checked cluster is
     * being merged into the closest cluster.
     * Ignoring default cluster for unclustered words and divergence between cluster and itself.
     */
    private boolean mergeCloseClusters() {
        boolean mergeDone = false;
        for (int clusterToCheck = START_OF_CLUSTERS; clusterToCheck < numOfClusters; clusterToCheck++) {
            if ((allClusters[clusterToCheck].isNewCluster()))
                continue;

            int closestCluster = UNCLUSTERED_INDEX;
            double closestDistance = MERGE_TRESHOLD;
            double tempDist;
            for (int otherCluster = START_OF_CLUSTERS; otherCluster < numOfClusters; otherCluster++) {
                if ((allClusters[clusterToCheck].isNewCluster()) || (clusterToCheck == otherCluster))
                    continue;

                try {
                    if ((tempDist = KLD.calculateDistance(allClusters[clusterToCheck], allClusters[otherCluster])) <= closestDistance) {
                        closestDistance = tempDist;
                        closestCluster = otherCluster;
                    }
                } catch (VectorException e) {
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                }
            } // inner for
            if (closestCluster != UNCLUSTERED_INDEX) {
                mergeClusters(closestCluster, clusterToCheck);
                mergeDone = true;
            }
        } // main for
        return mergeDone;
    }

    /**
     * find the closest cluster to some word after smoothing
     * @param wordToCluster a word to find its closest cluster.
     * @return an array assembled of [index of word, distance to closest cluster, closest cluster].
     * @throws VectorException
     */
    private Double[] findClosestClusterToWord(int wordToCluster) throws VectorException{
        Word word = null;
        try {
            word = Vocabulary.instance().getWordByIndex(wordToCluster);
            word.calculateContextDistribution();
        } catch (WordIsNotExsistException e) {
            e.printStackTrace();
        }
        int closestCluster = -1;
        double distanceToClosest = Integer.MAX_VALUE;
        for (int cluster = 1; cluster < allClusters.length; cluster++) {
            double distanceToCurrent = KLD.calculateDistance(word, allClusters[cluster]);
            if (distanceToCurrent < distanceToClosest) {
                distanceToClosest = distanceToCurrent;
                closestCluster = cluster;
            }
        }
        Double[] calculateToReturn = {(double)word.index, distanceToClosest, (double) closestCluster};
//        System.out.printf("word: [%s]\tclosest cluster[%s]\tdistance[%f]\n",
//                           word.getName(), allClusters[closestCluster].getName(), distanceToClosest);
        return calculateToReturn;
    }

    /**
     * iterate over all given words and find the closest cluster to each of them.
     * @param wordsToCluster a set of words that their closest cluster is to be found.
     * @return a set of double array contains details of the word, closest cluster and the distance.
     */
    private TreeSet<Double[]> findClosestClusterToAllWords(HashSet<Integer> wordsToCluster) {
        TreeSet<Double[]> calculatedClosestDistances = new TreeSet<>(new Comparator<Double[]>() {
            @Override
            public int compare(Double[] d1, Double[] d2) {
                return Double.compare(d2[1], d1[1]);
            }
        });

        Iterator<Integer> iterWords = wordsToCluster.iterator();
        while(iterWords.hasNext()) {
            try {
                calculatedClosestDistances.add(findClosestClusterToWord(iterWords.next()));
            }
            catch (VectorException e) {
                e.printStackTrace();
            }
        } // while
        return calculatedClosestDistances;
    }

    public void printClusters() {
        int index = 1;
        for (int clusterIndex = 1; clusterIndex < allClusters.length; clusterIndex++) {
            Cluster cluster = allClusters[clusterIndex];
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(pathToDir + "\\cluster " + index + ".txt")))) {
                bw.write(cluster.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

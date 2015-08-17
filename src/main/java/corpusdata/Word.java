package corpusdata;

import learner.StaticVariables;
import vectors_tools.Smoothing;

import java.util.ArrayList;

/**
 * class represents a word - contains its representing index, context distribution by words,
 * context distribution by clusters - which gets updated after each iteration, and the cluster the word is related to.
 * Created by Ron on 08/08/2015.
 */
public class Word extends Tagable {

    /* constatns */

    /* statics */
    public static int numOfWords = 0, numOfClusteredWords = 0;
    public static int numOfRareWords = 0;

    private static boolean isNumOfClustersSet = false;



    /* data members */
    private ArrayList<Integer[]> wordsContextDistribution; //each one in the array consist of word index and its count in the
    private int clusterTag;

    /*  constructors */
    /**
     * C'tor for word that have been watched for the first time while iterating
     * over some corpus.
     * @param wordName the name of the watched word.
     */
    public Word(String wordName, int index) {
        this.name = wordName;
        this.index = index;
        numOfWords++;
        numOfClusters = learner.StaticVariables.getNumOfClusters() + 1;
        this.frequency = 1;
        wordsContextDistribution = new ArrayList<>();
        clusterContextDistribution = new int[(int) Math.pow(numOfClusters, 2)];
        this.clusterTag = 0;
    }

    /**
     * C'tor for data from corpus that had been already parsed.
     * @param wordName the name of the word.
     * @param frequency number of times the word had been watched.
     */
    public Word(String wordName, int index,  int frequency, ArrayList<Integer[]> wordsContextDistribution) {
        this(wordName, index);
        this.frequency = frequency;
        if (frequency < StaticVariables.getRareWordTreshold())
            numOfRareWords++;
        this.wordsContextDistribution = wordsContextDistribution;
    }

    /**
     * set data members relevant to this word, after parsing them if word already added.
     * @param frequency the count of the word in the corpus.
     * @param wordsContextDistribution the context vector of the word from the corpus.
     */
    public void setWordData(int frequency, ArrayList<Integer[]> wordsContextDistribution) throws WordIsNotExsistException {
        this.frequency = frequency;
        if (frequency < StaticVariables.getRareWordTreshold())
            numOfRareWords++;
        this.wordsContextDistribution = wordsContextDistribution;
        Vocabulary.instance().setWordWithFrequency(this.index);
    }

    @Override
    public int hashCode() {
        return ((Integer) this.index).hashCode();
    }

    public ArrayList<Integer[]> getWordsContextDistribution() {
        return wordsContextDistribution;
    }

    public int getClusterTag() {
        return clusterTag;
    }

    public void setClusterTag(int clusterTag) {
        if (clusterTag != 0)
            numOfClusteredWords++;
        this.clusterTag = clusterTag;
    }

    /**
     * calculating the distribution of the context by clusters
     * and smoothing the results.
     */
    public void calculateContextDistribution() throws WordIsNotExsistException {
        buildClusterContextDistribution();
        Smoothing.smoothProbabilities(this);
    }

    /**
     * find in which contexts (by clusters) this word occurs
     * and count the number of occurrences in each context.
     * done by iterating over the context between words (from the corpus)
     * and  only if the word before and the word after already belong to some cluster,
     * the relevant context by clusters is being counted.
     */
    private void buildClusterContextDistribution() {
        for (Integer[] contextType : wordsContextDistribution) {
            int clusterBefore = 0, clusterAfter = 0;
            try {
                clusterBefore = Vocabulary.instance().getWordByIndex(contextType[0]).clusterTag;
                clusterAfter = Vocabulary.instance().getWordByIndex(contextType[1]).clusterTag;
            } catch (WordIsNotExsistException e) {
                e.printStackTrace();
                continue;
            }


            //adding the frequency of that context type to the frequency of the cluster context type
            if ((clusterBefore != 0) && (clusterAfter != 0)) {
                clusterContextDistribution[(clusterBefore * numOfClusters) + clusterAfter] += contextType[2];
            }
        }
    }
}

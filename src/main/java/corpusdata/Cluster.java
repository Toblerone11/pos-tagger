package corpusdata;

import vectors_tools.Smoothing;
import vectors_tools.VectorOperators;

import java.util.Comparator;
import java.util.TreeSet;

/**
 * class that represents a cluster with words that has similar distribution over the corpus
 * related to each word context.
 * Created by Ron on 09/08/2015.
 */
public class Cluster extends Tagable {

    /* constant */
    private static final int UNCLUSTERED_INDEX = 0;

    /* statics */
    private static int numOfClusters = 0;
    private static boolean unclusteredCreated = false;

    /**
     * static method in order to build only one cluster for unclustered words.
     *
     * @return cluster for unclustered words
     */
    public static Cluster initialClusterForUnclustered() {
        if (unclusteredCreated)
            return null;

        unclusteredCreated = true;
        return new Cluster();
    }

    /* data members */
    private TreeSet<Integer> words;
    private boolean newCluster;

    /* constructors */

    /**
     * private constructor onlu for the first cluster (cluster of unclustered words).
     */
    private Cluster() {
        initialWordsTreeSet();
        this.frequency = 0;
        this.index = numOfClusters;
        this.numOfClusters++;
        this.name = "Unclustered";
    }

    /**
     * C'tor - initializer of cluster with single word.
     *
     * @param word the first word to initial the cluster with
     */
    public Cluster(Word word) {
        this.index = numOfClusters;
        numOfClusters++;
        restartCluster(word);
    }

    /**
     * C'tor - initialzie cluster with another cluster
     *
     * @param mainCluster
     * @param clusters
     */
    public Cluster(Cluster mainCluster, Cluster... clusters) {
        this.name = mainCluster.getName();
        this.index = mainCluster.index;
        this.frequency = mainCluster.getFrequency();
        initialWordsTreeSet();
        words.addAll(mainCluster.words);
        for (Cluster c : clusters) {
            words.addAll(c.words);
            frequency += c.getFrequency();
        }
    }

    /* methods */

    /**
     * @return amount of words that belong to this cluster
     */
    public int size() {
        return words.size();
    }

    private void initialWordsTreeSet() {
        this.words = new TreeSet<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer word1, Integer word2) {
                int freq1 = 0;
                int freq2 = 0;
                try {
                    freq1 = Vocabulary.instance().getWordByIndex(word1).getFrequency();
                    freq2 = Vocabulary.instance().getWordByIndex(word2).getFrequency();

                } catch (WordIsNotExsistException e) {
                    e.printStackTrace();
                }
                return -Integer.compare(freq1, freq2);
            }
        });
    }

    /**
     * adding word to the set of words and its frequency in the corpus.
     *
     * @param word
     */
    public void addWord(Word word) {
        if (words.add(word.index)) {
            this.frequency += word.getFrequency();
            word.setClusterTag(this.index);

            this.newCluster = false;
            if (this.index != UNCLUSTERED_INDEX)
                Vocabulary.instance().removeClusteredWord(word.index);

//            if (this.index != 0) {      //remove only if not default cluster.
//                Integer[] wordByFrequencyToRemove = {word.index, word.getFrequency()};
//                Vocabulary.instance().removeClusteredWord(wordByFrequencyToRemove); //remove the word from the relevant set.
//            }
        }
    }

    /**
     * if some word need to be clustered. it has to be removed from the
     * cluster of unclustered words, or from its last cluster.
     *
     * @param word a word to be removed from the cluster.
     */
    public void removeWord(Word word) {
        words.remove(word.index);
        this.frequency -= word.getFrequency();

        if (this.index != UNCLUSTERED_INDEX)
            word.setClusterTag(UNCLUSTERED_INDEX);
        Vocabulary.instance().getAllUnclustererdWords().add(word.index);
        // adding to the Vocabulary to unclustered words.
        //for now it is not necessary.
    }

    /**
     * activating the removeWord if only word's index was given
     *
     * @param wordIndex
     */
    public void removeWord(int wordIndex) {
        try {
            removeWord(Vocabulary.instance().getWordByIndex(wordIndex));
        } catch (WordIsNotExsistException e) {
            e.printStackTrace();
        }
    }

    /**
     * add new word by its given representation value.
     *
     * @param wordRepr the represantation of the word as integer.
     */
    public void addWord(int wordRepr) {
        Word word;
        try {
            word = Vocabulary.instance().getWordByIndex(wordRepr);
            addWord(word);
        } catch (WordIsNotExsistException e) {
            e.printStackTrace();
        }
    }

    /**
     * adding set of words represented by their index value.
     *
     * @param wordsToAdd a set of words to add
     */
    public void addAllWords(TreeSet<Integer> wordsToAdd) {
        for (int wordRepr : wordsToAdd) {
            addWord(wordRepr);
        }
    }

    /**
     * operate a merging between two clusters where the argument cluster is added to this cluster.
     * recalculating distribution vector after the operation
     *
     * @param other a cluster to merge with this cluster.
     */
    public void mergeWithOtherCluster(Cluster other) {
        this.addAllWords(other.words);
        this.calculateDistribution();
    }

    /**
     * calculating the distribution of some cluster depend on the words that belong to it.
     * the calculation taking in consideration the frequency of each word as weight to determine the real distribution.
     * meaning that if
     */
    public void calculateDistribution() {
        try {
            clusterContextDistribution = new int[(int) Math.pow(numOfClusters, 2)];
            for (int wordIndex : words) {
                Word word = Vocabulary.instance().getWordByIndex(wordIndex);
                word.calculateContextDistribution();
                VectorOperators.addVectors(this.clusterContextDistribution, word.getClusterContextDistribution());
            }

            int clusterSize = this.size();
            for (int contextIndex = 0; contextIndex < Math.pow(numOfClusters, 2); contextIndex++) {
                clusterContextDistribution[contextIndex] =
                        (int) Math.floor(clusterContextDistribution[contextIndex] / clusterSize);
            }

            this.smoothedContexts = Smoothing.smoothProbabilities(this);
        } catch (WordIsNotExsistException e) {
            e.printStackTrace();
        }
    }

    /**
     * restart cluster with the next unclustered most frequent word.
     * used in case of merging between this cluster with another.
     */
    public void restartCluster(Word word) {
        initialWordsTreeSet();
        addWord(word);
        this.frequency = word.getFrequency(); //increase the frequency of the cluster by the frequency of the word
        this.name = word.getName();
        newCluster = true;
    }

    /**
     * check if some cluster is just created, therefore containing one word (and irrelevant for merging, for example).
     *
     * @return true if the cluster has just created, false otherwise.
     */
    public boolean isNewCluster() {
        return newCluster;
    }

    @Override
    public String toString() {
        String toReturn = "cluster name: " + this.getName() + '\n';

        toReturn += "\ncluster distribution:'\n'[";
        calculateDistribution();
        for (int x : clusterContextDistribution) {
            toReturn += x + ", ";
        }
        toReturn += "]\n";

        toReturn += "\ncontain words:\n";
        for (int word : words) {
            try {
                Word wordToPrint = Vocabulary.instance().getWordByIndex(word);
                toReturn += wordToPrint.getName() + " " + wordToPrint.getFrequency() + "\n";
            } catch (WordIsNotExsistException e) {
                e.printStackTrace();
            }
        }
        return toReturn;
    }

    public TreeSet<Integer> getWords() {
        return words;
    }

    public double[] getSmoothedContexts() {
        return smoothedContexts;
    }
}

package learner;

import corpusdata.Cluster;
import corpusdata.Vocabulary;
import corpusdata.Word;
import corpusdata.WordIsNotExsistException;
import vectors_tools.Smoothing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Ron
 */
public class Recorder {

    /* constants */
    private static final String CLUSTER_DIR = "clusterResults\\";
    /* data members */
    BufferedWriter bw;
    String pathToClusterDir;
    String pathToDir;


    public Recorder(String pathToDir) {
        bw = null;
        this.pathToDir = pathToDir;
        this.pathToClusterDir = pathToDir + CLUSTER_DIR;
        createNewResultsFolder();
    }

    public void createNewResultsFolder() {
        File newDir = new File(this.pathToClusterDir);
        if (newDir.exists()) {
            for(String file : newDir.list()) {
                File current = new File(newDir.getPath(), file);
                current.delete();
            }
            newDir.delete();
        }
        newDir.mkdirs();
    }

    public boolean writeClusterToFile(Cluster cluster) {
        File clusterFile = new File(pathToClusterDir + cluster.getName() + ".txt");
        try {
            clusterFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(clusterFile))) {

            bw.write(cluster.toString());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean writeLogToFile() {
        File log = new File(pathToDir + "log.txt");

        try {
            log.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(log))) {

            bw.write(String.format(
                    "number of words : %d\nnumber of rare words : %d\nnumber of clustered words : %d\n",
                    Word.numOfWords, Word.numOfRareWords, Word.numOfClusteredWords));
            bw.write("\nnumber of iterations : " + ClusterWords.numOfIterations + '\n');

            bw.write("\nrare words:\n");

            Iterator<Integer> rareWordsIter = Vocabulary.instance().getRareWords().iterator();
            int newLineMarker = 10;
            int wordsInLine = 0;
            try {
                while (rareWordsIter.hasNext()) {
                    bw.write(Vocabulary.instance().getWordByIndex(rareWordsIter.next()).getName() + " ");
                    wordsInLine++;
                    if (wordsInLine == newLineMarker)
                        bw.write('\n');
                }
            } catch (WordIsNotExsistException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                bw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * return an array with the most n frequent words in the given cluster.
     *
     * @param n            number of top frequent words.
     * @param clusterIndex the cluster to take the words from.
     * @return an array with the most n equent words from the given cluster.
     */
    private Integer[] getMostFrequentWordsInCluster(int n, int clusterIndex) {
        ArrayList<Integer> freqWords = new ArrayList<>();
        Integer[] wordsToReturn = new Integer[n];
        Iterator<Integer> wordsIter = ClusterWords.allClusters[clusterIndex].getWords().iterator();
        for (int i = 0; i < n; i++) {
            if (wordsIter.hasNext())
                freqWords.add(wordsIter.next());
            else
                break;
        }
        return freqWords.toArray(wordsToReturn);
    }

    /**
     * covert a vector of ints to String where indices seperated by spaces.
     *
     * @param wordIndex the word's distribution vector to convert
     * @return String represnt a vector.
     */
    private String wordDistributionToString(int wordIndex) {
        String distributionVector = "";
        double[] wordDist;
        try {
            wordDist = Smoothing.smoothProbabilities(
                    Vocabulary.instance().getWordByIndex(wordIndex));
        } catch (WordIsNotExsistException e) {
            e.printStackTrace();
            return null;
        }
        for (int context = 1; context < wordDist.length; context++) {
            distributionVector += String.format("%3f ", (wordDist[context]));
        }
        return distributionVector;
    }

    /**
     * record a matrix with n rows and m columns where m is the size of all clusters in the power of 2,
     * and n is the size of all clusters multiply by 10 (10 words from each clusters).
     *
     * @return
     */
    public boolean writeMatrixOfWords() {
        try {
            File wordsToShow = new File(pathToDir + "\\wordsToVisualize.txt");
            wordsToShow.createNewFile();

            bw = new BufferedWriter(new FileWriter(wordsToShow));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedWriter vtv = new BufferedWriter(new FileWriter(new File(pathToDir + "\\vectorsToVisualize.txt")))) {
            for (int i = 0; i < ClusterWords.allClusters.length; i++) {
                Integer[] wordsToWrite = getMostFrequentWordsInCluster(10, i);
                for (Integer word : wordsToWrite) {
                    if (word == null)
                        continue;
                    String toWrite = wordDistributionToString(word) + '\n';
                    if (toWrite != null) {
                        vtv.write(toWrite);
                        bw.write(Vocabulary.instance().getWordByIndex(word).getName() + '\n');
                    }
                }
                bw.write("endOfCluster\n");
            }
            bw.write("endOfWords\n");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (WordIsNotExsistException e) {
            e.printStackTrace();
        }

        return false;

    }
}

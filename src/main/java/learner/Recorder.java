package learner;

import corpusdata.Cluster;
import corpusdata.Vocabulary;
import corpusdata.Word;
import corpusdata.WordIsNotExsistException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by Ron
 */
public class Recorder {

    /* data members */
    BufferedWriter bw;
    String pathToDir;


    public Recorder(String pathToDir) {
        bw = null;
        this.pathToDir = pathToDir;
    }

    public boolean writeClusterToFile(Cluster cluster) {
        File clusterFile = new File(pathToDir + cluster.getName());
        try {
            clusterFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        try (BufferedWriter bw =new BufferedWriter(new FileWriter(clusterFile))) {

            bw.write(cluster.toString());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean writeLogToFile() {
        File log = new File(pathToDir + "log");

        try {
            log.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        try (BufferedWriter bw =new BufferedWriter(new FileWriter(log))) {

            bw.write(String.format(
                    "number of words : %d\nnumber of rare words : %d\nnumber of clustered words : %d\n",
                    Word.numOfWords, Word.numOfRareWords, Word.numOfClusteredWords));
            bw.write("\nnumber of iterations : " + ClusterWords.numOfIterations);

            Iterator<Integer> rareWordsIter = Vocabulary.instance().getRareWords().iterator();
            int newLineMarker = 10;
            int wordsInLine = 0;
            try {
                while(rareWordsIter.hasNext()) {
                    bw.write(Vocabulary.instance().getWordByIndex(rareWordsIter.next()).getName());
                    wordsInLine++;
                    if (wordsInLine == newLineMarker)
                        bw.newLine();
                }
            } catch (WordIsNotExsistException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}

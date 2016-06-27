package learner;

import com.sun.istack.internal.Nullable;
import com.sun.javaws.exceptions.InvalidArgumentException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import static learner.StaticVars.*;

/**
 * [pathToDir], [numOfClusters], [mergeTreshold], [rareTreshold], [clusterTreshold]
 * "/home/ec2-user/cluster_words/"
 * Created by Ron
 */
public class POSLearner {

    private static final String PYTHON_MAIN = "main.py",
                                PY_RESULTS = "python_preprocessing" + File.separator + "getResults.py";

    @Nullable
    private File getPythonExec(String pyName) {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(pyName).getFile());
    }

    public static void main(String[] args) throws IOException, InvalidArgumentException {

        int nClusters = 77, rareTreshold = 20;
        double similarClusters = 0.2, wordClusterSimilarity = 0.3;

        boolean continueParse = true;

        String pathToCorpusDir;
        try {
            pathToCorpusDir = args[0];
        } catch (IndexOutOfBoundsException e) {
            throw new InvalidArgumentException(args);
        }

        String pathToOutputDir;
        try {
            pathToOutputDir = args[1];
        } catch (IndexOutOfBoundsException e) {
            throw new InvalidArgumentException(args);
        }

        try {
            nClusters = Integer.parseInt(args[2]);
        } catch (IndexOutOfBoundsException e) {
            continueParse = false;
        }

        if (continueParse) {
            try {
                rareTreshold = Integer.parseInt(args[3]);
            } catch (IndexOutOfBoundsException e) {
                continueParse = false;
            }
        }

        if (continueParse) {
            try {
                similarClusters = Double.parseDouble(args[4]);
            } catch (IndexOutOfBoundsException e) {
                continueParse = false;
            }
        }

        if (continueParse) {
            try {
                wordClusterSimilarity = Double.parseDouble(args[5]);
            } catch (IndexOutOfBoundsException e) {

            }
        }

        // running python script first in order to preprocess the corpus and save details into files.
        File pyFile = (new POSLearner()).getPythonExec(PYTHON_MAIN);
        String pathToContext = pathToOutputDir + File.separator + NAME_OF_CONTEXT;
        String pathToDictionary = pathToOutputDir + File.separator + NAME_OF_DICTIONARY;
        String runCommand = String.format("python %s %s %s %s", pyFile, pathToCorpusDir, pathToContext, pathToDictionary);
        System.out.println(runCommand);
        Process pyProcess = Runtime.getRuntime().exec(runCommand);
        BufferedReader pyOutput = new BufferedReader(new InputStreamReader(pyProcess.getInputStream()));
        System.out.println(pyOutput.readLine());

        setNumOfClusters(nClusters);
        setRareWordTreshold(rareTreshold);
        setMergeTreshold(similarClusters);
        setWordToClusterTreshold(wordClusterSimilarity);

        ClusterWords learner = new ClusterWords(pathToCorpusDir, pathToOutputDir);
        learner.learnPOS();

        pyFile = (new POSLearner()).getPythonExec(PY_RESULTS);
        pyProcess = Runtime.getRuntime().exec(String.format("%s %s", pyFile.getAbsolutePath(), pathToOutputDir));
        pyOutput = new BufferedReader(new InputStreamReader(pyProcess.getInputStream()));
        System.out.println(pyOutput.readLine());
    }
}

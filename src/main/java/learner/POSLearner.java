package learner;

import java.io.*;

import static learner.StaticVars.*;

/**
 * [pathToDir], [numOfClusters], [mergeTreshold], [rareTreshold], [clusterTreshold]
 * "/home/ec2-user/cluster_words/"
 * Created by Ron
 */
public class POSLearner {

    private static final String PYTHON_MAIN = "main.py",
                                PY_RESULTS = "python_preprocessing" + File.separator + "getResults.py";

    private static final String NO_PATH_TO_CORPUS = "-";

    private File getPythonExec(String pyName) {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(pyName).getFile());
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        int nClusters = 77, rareTreshold = 20;
        double similarClusters = 0.2, wordClusterSimilarity = 0.3;

        boolean continueParse = true;

        String pathToCorpusDir;
        try {
            pathToCorpusDir = args[0];
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException();
        }

        String pathToOutputDir;
        try {
            pathToOutputDir = args[1];
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException();
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
        File pyFile;
        String runCommand;
        Process pyProcess;
        String outLine;
        BufferedReader pyOutput;

        if (!(pathToCorpusDir.equals(NO_PATH_TO_CORPUS))) {
            pyFile = (new POSLearner()).getPythonExec(PYTHON_MAIN);
            String pathToContext = pathToOutputDir + File.separator + NAME_OF_CONTEXT;
            String pathToDictionary = pathToOutputDir + File.separator + NAME_OF_DICTIONARY;
            runCommand = String.format("python %s %s %s %s", pyFile, pathToCorpusDir, pathToContext, pathToDictionary);
            System.out.println(runCommand);
            pyProcess = Runtime.getRuntime().exec(runCommand);
//        BufferedReader pyOutput = new BufferedReader(new InputStreamReader(pyProcess.getInputStream()));
            pyProcess.waitFor();
        }


        setNumOfClusters(nClusters);
        setRareWordTreshold(rareTreshold);
        setMergeTreshold(similarClusters);
        setWordToClusterTreshold(wordClusterSimilarity);

        ClusterWords learner = new ClusterWords(pathToCorpusDir, pathToOutputDir);
        learner.learnPOS();

        pyFile = (new POSLearner()).getPythonExec(PY_RESULTS);
        pyProcess = Runtime.getRuntime().exec(String.format("%s %s", pyFile.getAbsolutePath(), pathToOutputDir));
        pyOutput = new BufferedReader(new InputStreamReader(pyProcess.getInputStream()));
        while ((outLine = pyOutput.readLine()) != null) {
            System.out.println(outLine);
        }
    }
}

package learner;

import com.sun.istack.internal.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * [pathToDir], [numOfClusters], [mergeTreshold], [rareTreshold], [clusterTreshold]
 * "/home/ec2-user/cluster_words/"
 * Created by Ron
 */
public class POSLearner {

    /* statics */
    private static String pathToCorpusDir;
    private static String pathToOutputDir;

    private static final String PYTHON_MAIN = "python_preprocessing" + File.separator + "main.py";
    private static final String PREPROCESSED_DATA = "." + File.separator + "preprocessed_words";

    @Nullable
    private File getPythonExec() {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(PYTHON_MAIN).getFile());
    }

    public static void main(String[] args) throws IOException {

        // running python script first in order to preprocess the corpus and save details into files.
        File file = (new POSLearner()).getPythonExec();
        Process p = Runtime.getRuntime().exec(file.getAbsolutePath());
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        System.out.println(in.readLine());

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

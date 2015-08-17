package corpusdata;

import learner.StaticVariables;

/**
 * marker Interface - use to mark object as tagable - words and clusters
 * Created by Ron on 09/08/2015.
 */
public abstract class Tagable {

    /* constants */

    /* statics */


    /* data members */
    protected String name;
    public int index;         //the representation of the word as int
    protected int frequency;  //number of times the word had been watched
    protected int[] clusterContextDistribution;
    protected int numOfClusters;

    /**
     * Getter of the frequency of th word.
     * @return number of times the word shows up in the corpus.
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     * Getter for wordName
     * @return word's Name as String
     */
    public String getName() {
        return name;
    }

    /**
     * Getter of the distribution vector by clusters
     * @return vector of the distribution by clusters.
     */
    public int[] getClusterContextDistribution() {
        return clusterContextDistribution;
    }
}

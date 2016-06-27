package corpusdata;

import learner.StaticVars;

import java.util.*;

/**
 * Singelton - stores basic data about the words and their representation.
 * Created by Ron
 */
public class Vocabulary {

    /* constants */
    private static final String START = "^", END = "$";

    /* statics */
    private static Vocabulary vocabulary = null;

    /* data members */
    Iterator<Integer[]> frequencyIter;
    int currentStatusOfIterator;

    /* all words, represented by their index value, ordered by frequency.*/
    private TreeSet<Integer[]> wordsByFrequency;
    private HashMap<Integer, Word> allWordsByIndex;
    private HashSet<Integer> frequentWords;
    private HashSet<Integer> rareWords;

    /**
     * @param wordsDictionary
     */
    private Vocabulary(HashMap<Integer, Word> wordsDictionary) {
        /* ordering words from greatest to lowest */
        wordsByFrequency = new TreeSet<>(new Comparator<Integer[]>() {
            @Override
            public int compare(Integer[] wordFreq1, Integer[] wordFreq2) {
                int result = Integer.compare(wordFreq2[1], wordFreq1[1]);
                if (result == 0)
                    result = Integer.compare(wordFreq1[0], wordFreq2[0]);

                return result;
            }
        });

        this.allWordsByIndex = wordsDictionary;
        this.allWordsByIndex.put(0, new Word(START, 0));
        this.allWordsByIndex.put(1, new Word(END, 1));

        this.frequentWords = new HashSet<>();
        this.rareWords = new HashSet<>();

        frequencyIter = wordsByFrequency.iterator();
        currentStatusOfIterator = 0;
    }

    /**
     * getter of the single instance of this class
     *
     * @return Vocabulary single instance.
     */
    public static Vocabulary instance(HashMap<Integer, Word> wordsDictionary) {
        System.out.println(wordsDictionary.isEmpty());
        if (vocabulary == null) {
            vocabulary = new Vocabulary(wordsDictionary);
        }
        //System.out.println(vocabulary.getWordByIndex(3) == null);

        return vocabulary;
    }

    /**
     * return the vocabulary single instance only if it is already exist.
     *
     * @return
     */
    public static Vocabulary instance() {
        return vocabulary;
    }

    /**
     * return the Word object represented by the given index.
     *
     * @param index the index representing the desired word.
     * @return Word object related to the given index
     */
    public Word getWordByIndex(int index) throws WordIsNotExsistException {
        Word wordToReturn = allWordsByIndex.get(index);
        if (wordToReturn == null)
            throw new WordIsNotExsistException(index);

        return wordToReturn;
    }

    /**
     * adding array of size 2 with word representation and the word's frequency
     *
     * @param wordIndex the index of the word to add
     */
    public void setWordWithFrequency(int wordIndex) throws WordIsNotExsistException {
        int frequency = getWordByIndex(wordIndex).getFrequency();
        Integer[] toAdd = {wordIndex, frequency};
        this.wordsByFrequency.add(toAdd);

        if (frequency >= StaticVars.getRareWordTreshold()) {
            frequentWords.add(wordIndex);
        } else
            rareWords.add(wordIndex);
    }

    /**
     * finds the most frequent word which doesn't belong yet to any cluster.
     *
     * @return the most frequent word which is unclustered.
     */
    public Word getMostFrequentUnclusteredWord() {
        Word word = null;
        while (this.frequencyIter.hasNext()) {
            Integer[] mostFrequent = this.frequencyIter.next();
            word = allWordsByIndex.get(mostFrequent[0]);
            currentStatusOfIterator++;
            if (word.getClusterTag() == 0)
                return word;
        }
        return null;
    }

    /**
     * return all words in the frequent set. all those words are unclustered.
     *
     * @return set of frequent words
     */
    public HashSet<Integer> getFrequentWords() {
        return this.frequentWords;
    }

    /**
     * gather all words that are unclustered yet.
     *
     * @return subset of all words contains all unclustered words.
     */
    public HashSet<Integer> getAllUnclustererdWords() {
        return getAllUnclustererdWords(0);
    }

    /**
     * gather all words that are unclustered yet and occurs more times
     * than the given lower bound
     *
     * @param frequencyLowerBound the treshold which determine if some word is nor rare
     * @return a set of all words that are unclustered nor rare.
     */
    public HashSet<Integer> getAllUnclustererdWords(double frequencyLowerBound) {
        HashSet<Integer> unclustered = new HashSet<>();

        // use the frequency iterator from the last word that had been clustered to new Cluster.
        Word word = null;
        while (frequencyIter.hasNext()) {
            Integer[] nextWord = frequencyIter.next();
            if (nextWord[1] < frequencyLowerBound)
                break;

            unclustered.add(nextWord[0]);
        }
        setIteratorToLastPosition();
        return unclustered;
    }

    /**
     * after using the 'frequencyIter' becouse its currnt position
     * and not in order to cluster some frequent word.
     * this method reset it to the last marked position by
     * 'currentStatusOfIterator'.
     */
    private void setIteratorToLastPosition() {
        int index = 0;
        this.frequencyIter = wordsByFrequency.iterator();
        while (frequencyIter.hasNext()) {
            if (index == currentStatusOfIterator)
                break;
            this.frequencyIter.next();
            index++;
        }
    }

    /**
     * remove words that already been clustered from the sorted set of words by frequency.
     * they are not needed there anymore.
     *
     * @param wordWithFrequency the word and its frequency in an array to be removed
     */
    public void removeClusteredWord(int clusteredWord) {
        this.frequentWords.remove(clusteredWord);
    }

    public void restartIterator() {
        frequencyIter = wordsByFrequency.iterator();
    }

    public HashSet<Integer> getRareWords() {
        return rareWords;
    }
}

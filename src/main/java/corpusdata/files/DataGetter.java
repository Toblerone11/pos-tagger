package corpusdata.files;

import corpusdata.Vocabulary;
import corpusdata.Word;
import corpusdata.WordIsNotExsistException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * class that get data from files and create objects to represent the data. such as words
 * Created by Ron on 10/08/2015.
 */
public class DataGetter {

    /* constants */
    private static final int WORD_INDEX = 0, REPR_INDEX = 1, CONTEXT_INDEX = 1;
    private static final int BEFORE_INDEX = 0, AFTER_INDEX = 1, FREQUENCY_INDEX = 2;
    private static final String CONTEXT_SEPERATOR = " ";
    private static final Pattern dictinaryPatt = Pattern.compile("\\w+ \\d+(?:[\n\r]{0,2})?");
    private static final int DOT_PRINT_MARKER = 1000;
    private static final String DOT = ".";
    private static final String START_SENTENCE = "^", END_SENTENCE = "$";
    private static final Integer START_INDEX = 0, END_INDEX = 1;

    /* data members */
    private BufferedReader br;
    private String pathToDir; //the directory contains all the data.
    private static DataGetter dg = null;
//    private HashMap<Integer, String> dictionary;
    Vocabulary vocab;

    /* constructors */

    /**
     * C'tor of the single instance of that class.
     * @param pathToDir a path to the directory containing the necessary files.
     * @param dictFile the name of the dictionary file in the directory.
     * @param contextFile the name of the contexts file in the directory
     */
    private DataGetter(String pathToDir, String contextFile, String dictFile) {
        System.out.println("building dictionary and words contexts from file");

        this.pathToDir = pathToDir;
        try {
            this.br = new BufferedReader(
                    new FileReader(new File(pathToDir + contextFile)));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
//        this.dictionary = new HashMap<>();

        this.vocab = Vocabulary.instance(createDictionary(dictFile));
        readContexts();

        System.out.println("Done!");
    }

    /**
     * a method to get the single instance of that class.
     * @param pathToDir a path to the directory containing the necessary files.
     * @param dictFile the name of the dictionary file in the directory.
     * @param contextFile the name of the contexts file in the directory
     * @return the single instance initialized if was null, or as is if not.
     */
    public static DataGetter getInstance(String pathToDir, String dictFile, String contextFile) {
        if (dg == null) {
            return new DataGetter(pathToDir, dictFile, contextFile);
        }
        return dg;
    }

    /**
     * createing a dictionary from the earlier mapping stored in a file.
     * @param dictionaryFile the name of the file storing the word mapping.
     * @return a mapping from word name to integer representation.
     */
    private HashMap<Integer, Word> createDictionary(String dictionaryFile) {
        System.out.println("processing dictionary file");
        String pathToDictionaryFile = pathToDir + dictionaryFile;
        HashMap<Integer, Word> dictionary = new HashMap<>();

        int lineNum = 0;
        try (BufferedReader dr = new BufferedReader(new FileReader(new File(pathToDictionaryFile)))) {
            String line;
            while ((line = dr.readLine()) != null) {
                lineNum++;

                if (lineNum == DOT_PRINT_MARKER) {
                    System.out.print(DOT);
                    lineNum = 0; //restarting
                }

                if ((!dictinaryPatt.matcher(line).matches()))
                    continue;

                String[] wordRepr = line.split(" ");
                Word newWord = new Word(wordRepr[WORD_INDEX], Integer.parseInt(wordRepr[REPR_INDEX]));
                // adding the word to the vocabulary dictionary
                dictionary.put(newWord.index, newWord);

                // adding temp dictionary from parsed corpus
//                this.dictionary.put(newWord.index, newWord.getName());
            }

        }
        catch (IOException e) {
            System.err.println(lineNum);
            e.printStackTrace();
        }
        dictionary.put(START_INDEX, new Word(START_SENTENCE, START_INDEX));
        dictionary.put(START_INDEX, new Word(END_SENTENCE, END_INDEX));
        System.out.println("Done!");
        return dictionary;
    }

    /**
     *
     * @return
     */
    private void readContexts() {
        System.out.println("processing words contexts file");
        String line = null;
        int lineNum = 0;
        int word = 0, before, after;
        int contextFrequency;
        try {
            int frequency;
            while ((line = br.readLine()) != null) {
                lineNum++;

                if (lineNum == DOT_PRINT_MARKER) {
                    System.out.print(DOT);
                    lineNum = 0; //restarting
                }

                frequency = 0;
                String[] wordLine = line.split(",");
                word = Integer.parseInt(wordLine[WORD_INDEX]);
                ArrayList<Integer[]> contextTypes = new ArrayList<>();
                for (int i = 1; i < wordLine.length; i++) {
                    String[] contextWords = wordLine[i].split(CONTEXT_SEPERATOR);
                    before = Integer.parseInt(contextWords[BEFORE_INDEX]);
                    after = Integer.parseInt(contextWords[AFTER_INDEX]);
                    contextFrequency = Integer.parseInt(contextWords[FREQUENCY_INDEX]);
                    Integer[] contextType = {before, after, contextFrequency};
                    contextTypes.add(contextType);

                    frequency += contextFrequency;
                }

                try {
                    vocab.getWordByIndex(word).setWordData(frequency, contextTypes);
                } catch (WordIsNotExsistException e) {
                    e.printStackTrace();
                    System.err.println("word hasn't added to dictionary");
                }
            }
        }
        catch (IOException e) {
            System.err.println("exception in line number: " + lineNum);
            e.printStackTrace();
        }
        System.out.println("Done!");
    }

}


import re, sys
import string
# from nltk.book import gutenberg
from nltk.corpus import brown
import nltk.corpus.reader.childes

from nltk.util import ngrams

__author__ = 'Ron'

num_of_words = 2
words = dict()
words["^"] = 0
words["$"] = 1
words_context = dict()

# NAME_OF_CORPUS = "corpus.txt"
legal_word = re.compile("([\w]+|\^|\$)")

def activate_with_progress(func, generator, max_size, elements_name):
    """
    this function wraps any other function which needs to be inspected with progress bar and prints progress bar
    to the comand prompt.
	:param func: gets one element which the generator outputs, return None (void)
    :param generator: a generator type to iterate over its elements,
    :param max_size: the amount of data the generator may yield.
    :param elements_name: str, the name of each element, used for the recording data.
    """
    post_count = 0
    percentage = int(max_size / 100)
    percent_count = 0
    print("num of all %s: %d\npercentage: %d\n" % (elements_name, max_size, percentage))
    sys.stdout.write("\r[%s%s] %d%s" % ('#' * percent_count, ' ' * (100 - percent_count), percent_count, '%'))
    sys.stdout.flush()

    has_next = True
    while has_next:
        try:
            func(generator.__next__())
            post_count += 1
            if post_count == percentage:
                percent_count += 1
                post_count = 0
                sys.stdout.write(
                        "\r[%s%s] %d%s" % ('#' * percent_count, ' ' * (100 - percent_count), percent_count, '%'))
                sys.stdout.flush()
                if percent_count == 62:
                    print('', end='')

                    # if percent_count == 80:
                    #     break
        except StopIteration:
            has_next = False
            pass
    print('\n')

	
def parseSentence(sentence):
    """
    gets prepared sentence without redundunt symbols
    only words and numbers.
    :param sentence: list of strings
    :return:
    """
    global num_of_words, words, words_context
    for sequence in ngrams(sentence, 3):
        for word in sequence:
            if word not in words:
                words[word] = num_of_words
                num_of_words += 1

        currentWord = words[sequence[1]]
        before = words[sequence[0]]
        after = words[sequence[2]]
        if currentWord in words_context:
            if (before, after) in words_context[currentWord]:
                words_context[currentWord][(before, after)] += 1
            else:
                words_context[currentWord][(before, after)] = 1
        else:
            words_context[currentWord] = dict({((before, after), 1)})


def readSentence(sentence, table):
    """
    parsing on sentence, adding start and end symbols.
    then creating n grams and adds them to the context dictionary
    :param sentence: a string line to be parsed
    :param table: tokens to get rid of.
    :param numOfLines: upper boundary to number of lines to be parsed.
    """
    sentence = sentence.translate(table)
    sentence = "^ " + sentence[:-1] + " $"  # add begining and end of sentence.
    sentence = sentence.lower()

    global num_of_words, words, words_context, legal_word
    stop_current_iter = False
    for sequence in ngrams(sentence.split(" "), 3):
        legal_sequence = []  # store legal form of each word during examining the sequence.
        for word in sequence:
            try:
                legal_sequence.append(legal_word.search(word).group())  # isolating word from redundant symbols
            except AttributeError:
                stop_current_iter = True
                break

        if (stop_current_iter):
            stop_current_iter = False
            break

        for word in legal_sequence:
            if word not in words:
                words[word] = num_of_words
                num_of_words += 1

        # if len(legal_sequence) < 3: # if not all words are legal
        #     continue
        sequence = legal_sequence

        currentWord = words[sequence[1]]
        before = words[sequence[0]]
        after = words[sequence[2]]
        if currentWord in words_context:
            if (before, after) in words_context[currentWord]:
                words_context[currentWord][(before, after)] += 1
            else:
                words_context[currentWord][(before, after)] = 1
        else:
            words_context[currentWord] = dict({((before, after), 1)})


def iterate_simple_file(name_of_corpus, num_of_lines=float('inf')):
    table = {ord(x): "" for x in string.punctuation}  # tokens to strip from the corpus.
    with open(name_of_corpus, 'r', 1000) as corpus:
        line = corpus.readline()
        lineNum = 0
        while line != "" and lineNum < num_of_lines:
            yield line
            # readSentence(line, table)
            line = corpus.readline()
    print("Done!")


def iterate_wiki_corpus(name_of_corpus):
    print("parsing wikipedia corpus")
    start_pattern = re.compile("<doc[ \"=\w]+>")
    end_pattern = re.compile("(See also.|References.|External links.|ENDOFARTICLE.)")
    separator = re.compile("[\.?:!-]")
    with open(name_of_corpus, 'r', 1000) as corpus:

        line = corpus.readline()
        while (line != None):
            if (start_pattern.fullmatch(line)):
                line = corpus.readline()
                while not end_pattern.fullmatch(line):
                    for sentence in re.split(separator):
                        readSentence(sentence, table)
                    line = corpus.readline()
    print("Done!")


def iterate_brown_corpus():
    print("parsing brown corpus")
    for fileid in brown.fileids():
        print("parse - brown: " + fileid)
        sentence = ["^"]
        for sent in brown.sents(fileid):
            for word in sent:
                legal_form = legal_word.search(word.lower())
                if (legal_form):
                    sentence.append(legal_form.group())

            sentence.append("$")
            parseSentence(sentence)
            sentence = ["^"]


def reverse_dict(dic):
    """
    replace between related keys and values.
    values becoming the keys, and the keys are becoming the values.
    :param dic: a dictionary to reverse
    :return: reversed dictionary
    """
    return dict({(dic[key], key) for key in dic})


def context2file(pathname_of_context):
    words_to_remove = []
    with open(pathname_of_context, 'w+') as allWords:
        for word in words:
            if word == "$" or word == "^":
                continue

            word_repr = words[word]
            row = ""
            try:
                for context in words_context[word_repr]:
                    row += "," + str(context[0]) + " " + str(context[1]) + " " + str(words_context[word_repr][context])
                if not row == "":
                    allWords.write(str(word_repr) + row + "\n")
            except KeyError:
                words_to_remove.append(word)

    print("number of words to remove: %r" % str(len(words_to_remove)))
    # for word in words_to_remove:
    #     del words[word]


def words2file(pathname_of_words):
    accept_word_pattern = re.compile("[\w]+")
    print("writing dictionary to file")
    print("number of words: " + str(len(words)))
    writeDotMarker = len(words) / 100
    wordNo = 0
    with open(pathname_of_words, 'w+') as dictFile:
        for word in words:
            wordNo += 1
            if (wordNo == writeDotMarker):
                wordNo, writeDotMarker = 0, 0
                print(".", end="")
                try:
                    key_value_toWrite = word + " " + str(words[word]) + '\n'
                    if accept_word_pattern.match(key_value_toWrite):
                        dictFile.write(key_value_toWrite)
                except UnicodeEncodeError:
                    key_value_toWrite = None
                else:
                    key_value_toWrite = None

def parseOnCorpus(path_to_corpus, path_to_context, path_to_words):
    it = iterate_simple_file(path_to_corpus)
    def closure_func(table):
        def parseSent(sentence):
            readSentence(sentence, table)
		
        return parseSent
	
    table = {ord(x): "" for x in string.punctuation}  # tokens to strip from the corpus.
    func = closure_func(table)
    print("parsing file: " + path_to_corpus)
    activate_with_progress(func, it, 8820000, "sentence")
	
    iterate_brown_corpus()
    context2file(path_to_context)
    words2file(path_to_words)

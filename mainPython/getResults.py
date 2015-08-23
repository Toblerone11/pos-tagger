import os
import random
import re
import numpy as np
from sklearn.decomposition import PCA

__author__ = 'Ron'

word_with_frequency = re.compile("([\w]+) (\d+)")


def list2String(lis):
    return "".join([word[0] + ", " for word in lis])


def getMostFrequentWordsInCluster(pathToFile):
    mostTenFrequentWords = []
    with open(pathToFile, 'r') as clusterFile:
        line = "";
        while line != "contain words:\n":
            line = clusterFile.readline()

        for word in range(10):
            line = clusterFile.readline()
            if (line == ""):
                break;
            line = word_with_frequency.match(line)
            frequent_word = (line.group(1), line.group(2))
            mostTenFrequentWords.append(frequent_word)

        while line != "":
            line = clusterFile.readline()

            line = word_with_frequency.match(line)
            if (line != None):
                frequent_word = (line.group(1), line.group(2))

            for idx in range(10):
                if (eval(frequent_word[1]) > eval(mostTenFrequentWords[idx][1])):
                    mostTenFrequentWords[idx] = frequent_word
                    break;

            line = clusterFile.readline()

        return mostTenFrequentWords


def getResultsFromAllFiles(pathToDir):
    with open(pathToDir + "- results.txt", 'w') as resultFile:
        if os.path.isdir(pathToDir):
            for subdir, dirs, files in os.walk(pathToDir):
                for file in files:
                    print(file)
                    toWrite = file[:-4] + "\t:\t"
                    toWrite += list2String(getMostFrequentWordsInCluster(subdir + os.sep + file))
                    resultFile.write(toWrite + '\n')


def choose_random_color():
    r = lambda: random.randint(0, 255)
    return '#%02X%02X%02X' % (r(), r(), r())


def reduceWordsVectorDimension(pathToDir):
    print("reducing dimension for chosen words")
    X = np.loadtxt(pathToDir + os.sep + "vectorsToVisualize.txt", ndmin=2)
    pca = PCA(n_components=2)
    pca.fit(X)
    return pca.transform(X)


def create_word_circle(word_id, x, y, color):
    return "<div class=\"circle\"" \
           "id=\"" + word_id + "\"" + \
           "onmouseover=\"showWord(this)\" onmouseout=\"hideWord()\"" + \
           "style=\"" + \
           "left:" + str(abs(x) * 3000) + "px;" + \
           "top:" + str(abs(y) * 3000) + "px;" + \
           "background:" + color + ";\"></div>\n"


def create_html_visualization(pathToDir):
    print("building html page")
    words_vectors = reduceWordsVectorDimension(pathToDir)
    with open(pathToDir + os.sep + "visualization.html", 'w+') as html:
        with open(pathToDir + os.sep + "wordsToVisualize.txt", 'r') as words_names:

            startHtml = "<!DOCTYPE html>\n<html>\n<head>\n" \
                        "<link rel=\"stylesheet\" type=\"text/css\" href=\"stylesheet.css\"></head>\n" \
                        "<body>\n\t<div class=\"vertLine\"></div>\n\t<div class=\"horLine\"></div>\n"

            endHtml = "\t<div class=\"word\"><p id=\"wordName\"></p></div>\n\t<script>\n\t\tfunction showWord(obj) {\n\t\t\t" \
                      "var word = obj.id;\n\t\t\tdocument.getElementById(\"wordName\").innerHTML = word;\n\t\t}\n\t\t" \
                      "function hideWord(obj) {\n\t\t\tdocument.getElementById(\"wordName\").innerHTML = \"\";\n\t\t}</script>"

            word_circles = ""

            word_id = words_names.readline()
            word_index = 0;
            while word_id[:-1] != "endOfWords":
                current_color = choose_random_color();
                while word_id[:-1] != "endOfCluster":
                    # print(word_id[:-1])
                    word_circles += create_word_circle(word_id[:-1], words_vectors[word_index][0],
                                                       words_vectors[word_index][1], current_color)
                    word_id = words_names.readline()
                    word_index += 1
                word_id = words_names.readline()

            print("finish")
            html_page = startHtml + word_circles + endHtml
            html.write(html_page)
            print("Done!")
            return

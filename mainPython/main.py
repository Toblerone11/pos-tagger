import os
import shutil
import sys
import corpusParser
import subprocess
import re
import getResults

__author__ = 'Ron'
usage = "Usage:\n[path to corpus] [path to output directory] [number of clusters] " \
        "[rare word treshold] [close clusters treshold] [close word and cluster treshold]"

path_to_corpus = ""
path_to_out_dir = ""
num_of_clusters = 77
rare_word_treshold = 20
close_clusters_treshold = 0.2
close_word_to_cluster = 0.3


def parse_args(argv):
    global path_to_corpus, path_to_out_dir, num_of_clusters, rare_word_treshold, \
        close_clusters_treshold, close_word_to_cluster
    # path to corpus
    if not os.path.isdir(argv[1]):
        raise ValueError("argument 1: path to corpus - the path doesn't exsist\n" + usage)
    else:
        path_to_corpus = argv[1]

    if not os.path.isdir(argv[2]):
        try:
            os.mkdir(argv[2])
        except:
            raise ValueError("argument 2: path to output directory - the path doesn't exsist\n" + usage)
    path_to_out_dir = argv[2]

    if len(argv) >= 4:
        if float(argv[4]) == int(argv[4]):
            num_of_clusters = int(argv[3])
        else:
            raise ValueError("argument 3: number of clusters - should be int\n" + usage)

    if len(argv) >= 5:
        if float(argv[4]) == int(argv[4]):
            rare_word_treshold = int(argv[4])
        else:
            raise ValueError("argument 4: rare word treshold - should be int\n" + usage)

    if len(argv) >= 6:
        close_clusters_treshold = float(argv[5])

    if len(argv) >= 7:
        close_word_to_cluster = float(argv[6])

    return


def main():
    parse_args(sys.argv)

    corpusParser.parseOnCorpus()
    print(path_to_out_dir)

    subprocess.call(['java', '-Xms512m', '-Xmx1024m', '-jar',
                     '.\posta-1.0-SNAPSHOT.jar', str(path_to_corpus),
                     str(path_to_out_dir), str(num_of_clusters), str(rare_word_treshold),
                     str(close_clusters_treshold), str(close_word_to_cluster)])

    getResults.getResultsFromAllFiles(path_to_out_dir)
    getResults.create_html_visualization(path_to_out_dir)
    getResults.place_css_file(path_to_out_dir)
    os.startfile(path_to_out_dir + os.sep + "visualization.html")

    return


if __name__ == '__main__':
    main()

import os
import subprocess
import sys
from corpusParser import parseOnCorpus

# from src.resources import corpusParser, getResults

__author__ = 'Ron'
usage = "Usage:\n[path to corpus] [path to output directory] [number of clusters] " \
        "[rare word treshold] [close clusters treshold] [close word and cluster treshold]"

path_to_corpus = ""
path_to_context = ""
path_to_words = ""


def parse_args(argv):
    global path_to_corpus, path_to_out_dir, num_of_clusters, rare_word_treshold, \
        close_clusters_treshold, close_word_to_cluster
    # path to corpus
    path_to_corpus = argv[1]
    path_to_context = argv[2]
    path_to_words = argv[3]

def main():
    parse_args(sys.argv)

    parseOnCorpus(path_to_corpus, path_to_words, path_to_context)
    print(path_to_out_dir)

    # subprocess.call(['java', '-Xms512m', '-Xmx1024m', '-jar',
    #                  '.\posta-1.0-SNAPSHOT.jar', str(path_to_corpus),
    #                  str(path_to_out_dir), str(num_of_clusters), str(rare_word_treshold),
    #                  str(close_clusters_treshold), str(close_word_to_cluster)])

    getResults.getResultsFromAllFiles(path_to_out_dir)
    getResults.create_html_visualization(path_to_out_dir)
    getResults.place_css_file(path_to_out_dir)
    os.startfile(path_to_out_dir + os.sep + "visualization.html")

    return


if __name__ == '__main__':
    main()

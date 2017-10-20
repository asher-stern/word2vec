

import numpy as np


def load_word_map(filename, maximum_number_of_words):
    with open(filename) as f:
        l = f.read().splitlines()
        wordToIndex = dict()
        indexToWord = dict()
        wordToIndex['__UNKNOWN__'] = 0
        indexToWord[0] = '__UNKNOWN__'
        if len(l) < maximum_number_of_words:
            maximum_number_of_words = len(l)
        for index in range(1,maximum_number_of_words+1):
            if l[index-1] == '':
                break
            wordToIndex[l[index-1]] = index
            indexToWord[index] = l[index-1]
        return (wordToIndex, indexToWord)


def vector_difference(vector1, vector2):
    return np.sum(np.square(vector1 - vector2))


def sort_by_similarity(vector, matrix):
    """
    Returns as list of indices of the matrix rows, sorted by their similarity to the given vector.
    :param vector: a numpy vector
    :param matrix: a numpy matrix
    :return: list of sorted indices
    """
    matrix_as_list = list(matrix)
    matrix_as_dict = {index:row for (index,row) in zip(range(len(matrix_as_list)),matrix_as_list)}
    return sorted(range(len(matrix_as_dict)), key=lambda k: vector_difference(vector, matrix_as_dict[k]))


def read_file_in_loop(f, number_of_lines):
    ret = list()
    for _ in range(number_of_lines):
        line = f.readline().strip()
        if line == '':
            f.seek(0)
            line = f.readline().strip()
        ret.append(line)
    return ret


if __name__ == '__main__':
    with open('/home/asher/main/data/business/d2bot/working/pre_processed/fraction_0.1/nisuy.txt') as f:
        print read_file_in_loop(f, 10)
        print read_file_in_loop(f, 10)
        print read_file_in_loop(f, 10)
        r = read_file_in_loop(f, 10)
        for x in r:
            (word, context) = x.split()
            print 'word = ', word, 'context = ', context


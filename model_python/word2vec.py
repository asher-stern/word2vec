import helper
import random
import tensorflow as tf
import numpy as np


# Constants

# These constants should correspond to the command line arguments given to neighbors generation program.
maximum_number_of_words = 10000
number_of_stop_words = 40
word_file = '/home/asher/main/data/word2vec_working_directory/preprocess/bnc_0.1/words.txt'
neighbors_file = '/home/asher/main/data/word2vec_working_directory/preprocess/bnc_0.1/neighbors.txt'

# Constants related to the model
vector_size = 30
negative_examples = 9 # So the total is 10
batch_size = 1000
number_of_batches = 16000
learning_rate = 0.1


def model_index(index):
    return index-(number_of_stop_words+1) # also for __UNKNOWN__


def word_index(index):
    return index+(number_of_stop_words+1) # also for __UNKNOWN__


(wordToIndex, indexToWord) = helper.load_word_map(word_file, maximum_number_of_words)

# This is the number of words in the model
number_of_words = len(wordToIndex)-(number_of_stop_words+1) # also for __UNKNOWN__


def similar_words(word, matrix):
    given_word_index = model_index(wordToIndex[word])
    given_word_vector = list(matrix)[given_word_index]
    sorted_row_indexes = helper.sort_by_similarity(given_word_vector, matrix)
    sorted_words = [indexToWord[word_index(i)] for i in sorted_row_indexes]
    return sorted_words


def generate_example(word, context):
    """
    Gets two indexes ("model indexes") and generates two lists. One with the given "word".
    The other is a list whose first element is "context", and the rest are random.
    :param word:
    :param context:
    :return:
    """
    word_list = [word]
    for _ in range(negative_examples):
        word_list.append(word)

    context_list = [context]
    for _ in range(negative_examples):
        context_list.append(random.randrange(number_of_words))

    return (word_list, context_list)


def generate_batch(neighbors_file):
    """
    Builds batch_size examples. Each example is 10 pairs (assuming negative_examples=9).
    The first pair is a word and a correct context.
    In the next 9 pairs, the word is the same word as the first, and the context is randomly picked.
    The words and the contexts are given as word-IDs.
    :param neighbors_file: the file neighbors.txt, which contains word-context pairs
    :return: two lists of the same size. List of words (in which each word repeats 10 times
    consequently), and a list of contexts (in which the first context is correct, and the following
    9 contexts are randomly picked). The lists' size is batch_size*(1+negative_examples).
    """
    batch_word_list = list()
    batch_context_list = list()
    positive_examples = helper.read_file_in_loop(neighbors_file, batch_size)
    for example in positive_examples:
        (word, context) = example.split()
        (word_list, context_list) = generate_example(model_index(int(word)), model_index(int(context)))
        batch_word_list.extend(word_list)
        batch_context_list.extend(context_list)
    return (batch_word_list, batch_context_list)

def generate_one_followed_by_zeros():
    ret = [1.0]
    for _ in range(negative_examples):
        ret.append(0.0)
    return np.array(ret)


one_followed_by_zeros = tf.constant(generate_one_followed_by_zeros(), tf.float32)

vectors = tf.Variable(tf.random_normal([number_of_words, vector_size]))
weights = tf.Variable(tf.random_normal([number_of_words, vector_size]))

words_ph = tf.placeholder(tf.int32, shape=[None])
context_ph = tf.placeholder(tf.int32, shape=[None])

word_vectors = tf.nn.embedding_lookup(vectors, words_ph)
context_vectors = tf.nn.embedding_lookup(weights, context_ph)

inner_products = tf.reduce_sum(word_vectors * context_vectors, axis=1)
per_word_inner_products = tf.reshape(inner_products, shape=[-1, negative_examples+1])
per_word_softmax = tf.nn.softmax(per_word_inner_products)
loss = tf.reduce_sum(tf.square(per_word_softmax - one_followed_by_zeros))

train_op = tf.train.GradientDescentOptimizer(learning_rate=learning_rate).minimize(loss)

init_op = tf.global_variables_initializer()


if __name__ == '__main__':
    with open(neighbors_file) as nf:
        with tf.Session() as session:
            session.run(init_op)
            for batch_number in range(number_of_batches):
                (batch_word_list, batch_context_list) = generate_batch(nf)
                feed_dict = {words_ph: batch_word_list, context_ph: batch_context_list}
                (_vectors, _, _loss) = session.run([vectors, train_op, loss], feed_dict=feed_dict)
                if 0==(batch_number % 50):
                    print batch_number, ": ", _loss
                    print similar_words('year', _vectors)[0:20]
                    print similar_words('development', _vectors)[0:20]

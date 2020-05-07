import re
import gensim
import logging
import numpy as np
import pyLDAvis.gensim

from WeightType import WeightType
from Clustering import Clustering
from nltk.tokenize import RegexpTokenizer
from stop_words import get_stop_words
from nltk.stem.porter import PorterStemmer
from gensim import corpora, models
from StringUtils import StringUtils


def best_match_between_topics(topics_a, topics_b):

    # print(topics_a)
    # print(topics_b)
    # print()

    word_match = []
    if not topics_a or not topics_b:
        return None

    for a in topics_a:
        for b in topics_b:
            if a[1] == b[1]:
                word_match.append(((float(a[0]) + float(b[0]) / 2), a[1]))

    word_match.sort(key=lambda x: x[0], reverse=True)
    return word_match[0] if len(word_match) > 0 else None


def apply_lda_to_text(docs, class_visitors):

    tokenizer = RegexpTokenizer(r'\w+')

    # create English stop words list
    en_stop = get_stop_words('en')

    # Create p_stemmer of class PorterStemmer
    p_stemmer = PorterStemmer()

    # Clean text based on java stop words

    docs = [StringUtils.clear_java_words(doc) for doc in docs]
    logging.info(docs)

    # compile sample documents into a list
    doc_set = docs

    # list for tokenized documents in loop
    texts = []

    # loop through document list
    for text in doc_set:
        # clean and tokenize document string
        raw = text.lower()
        tokens = tokenizer.tokenize(raw)
        # remove stop words from tokens
        stopped_tokens = [t for t in tokens if not t in en_stop]

        # stem tokens
        stemmed_tokens = [p_stemmer.stem(st) for st in stopped_tokens]

        # add tokens to list
        texts.append(stemmed_tokens)

    # turn tokenized documents into a id <-> term dictionary
    dictionary = corpora.Dictionary(texts)

    # filter dictionary from outliers
    dictionary.filter_extremes(no_below=5, no_above=0.8, keep_n=10000)

    # convert tokenized documents into a document-term matrix
    corpus = [dictionary.doc2bow(text) for text in texts]

    # generate LDA model
    ldamodel = gensim.models.ldamodel.LdaModel(
        corpus, num_topics=4, id2word=dictionary, passes=20)

    topics_per_doc = [ldamodel.get_document_topics(corp) for corp in corpus]

    # print(ldamodel.show_topics())
    # data = pyLDAvis.gensim.prepare(ldamodel, corpus, dictionary)
    # pyLDAvis.show(data)

    return topics_per_doc


def set_weight_for_clustering(graph, class_visitors, topics_per_doc):

    class_topics = {z[0]: z[1]
                    for z in zip(class_visitors.keys(), topics_per_doc)}

    k = 4
    for src, dst in graph.edges():
        src_vector = topics_vector(class_topics[src], k)
        dst_vector = topics_vector(class_topics[dst], k)

        similarity = Clustering.cosine_similarity(src_vector, dst_vector)
        graph[src][dst][str(WeightType.LDA)] = similarity
        print(f" {src} -> {dst} similarity of {similarity}")


def topics_vector(topics, k):
    dict_topics = {t[0]: t[1] for t in topics}
    vector = []
    for i in range(0, k):
        if i in dict_topics:
            vector.append(dict_topics[i])
        else:
            vector.append(0)

    return np.array(vector)

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


def apply_lda_to_classes(graph, classes, num_topics, pre_process=False):
    # classes {class_name : Class}
    docs = []
    print(f"CLASSESAPPLY {classes}")

    # Remove loose sections of the graph
    if pre_process:
        classes_to_remove = []
        for cla in classes:
            if cla not in graph.nodes():
                classes_to_remove.append(cla)
        for cla in classes_to_remove:
            classes.pop(cla, None)

    # (class_name, BOW)
    docs = [cla.get_merge_of_strings()
            for cla in classes.values()]

    print(f"DOCS: {docs}")
    lda_result = apply_lda_to_text(docs, num_topics)
    print(f"LDA Result: {lda_result}")

    services = {}
    for class_name, topics in zip(classes.keys(), lda_result):
        print(f"{class_name} -> {topics}")

        topic_number = 0
        topic_value = 0
        for topic in topics:
            if topic[1] > topic_value:
                topic_value = topic[1]
                topic_number = topic[0]

        if topic_number in services:
            services[topic_number].append((class_name, topics))
        else:
            services[topic_number] = [(class_name, topics)]

    set_weight_for_clustering(
        graph, classes, lda_result, num_topics)

    with open("./services_lda.txt", "w") as f:
        for service in services:
            f.write(f"{service}\n")
            for classe in services[service]:
                f.write(f"{classe}\n")
            f.write("\n")

    # colors = Clustering.create_colors(services.values())
    # Graph.draw(graph, colors)

    # Create cluster string to measure metrics
    clusters = []
    for service in services:
        cluster = []
        for classe in services[service]:
            cluster.append(classe[0])
        clusters.append(cluster)

    print(f"CLUSTERSLDA: {clusters}")

    return clusters


def apply_lda_to_text(docs, num_topics):

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
    dictionary.filter_extremes(no_below=3, no_above=0.8, keep_n=10000)

    # convert tokenized documents into a document-term matrix
    corpus = [dictionary.doc2bow(text) for text in texts]

    # generate LDA model
    # TODO : load a gensim model
    # https://radimrehurek.com/gensim/models/ldamodel.html
    # ldamodel = LdaModel.load()

    ldamodel = gensim.models.ldamodel.LdaModel(
        corpus, num_topics=num_topics, id2word=dictionary, passes=50)

    topics_per_doc = [ldamodel.get_document_topics(corp) for corp in corpus]

    print("\n\n\nShowing topics")
    print(ldamodel.show_topics(num_topics=num_topics, num_words=5, formatted=True))
    # print(ldamodel.show_topics())
    # data = pyLDAvis.gensim.prepare(ldamodel, corpus, dictionary)
    # pyLDAvis.show(data)

    return topics_per_doc


def set_weight_for_clustering(graph, class_visitors, topics_per_doc, k):

    class_topics = {z[0]: z[1]
                    for z in zip(class_visitors.keys(), topics_per_doc)}

    for src, dst in graph.edges():
        try:

            src_vector = topics_vector(class_topics[src], k)
            dst_vector = topics_vector(class_topics[dst], k)

            similarity = Clustering.cosine_similarity(src_vector, dst_vector)
            graph[src][dst][str(WeightType.LDA)] = similarity
            print(f" {src} -> {dst} similarity of {similarity}")
        except KeyError:
            pass


def topics_vector(topics, k):
    dict_topics = {t[0]: t[1] for t in topics}
    vector = []
    for i in range(0, k):
        if i in dict_topics:
            vector.append(dict_topics[i])
        else:
            vector.append(0)

    return np.array(vector)

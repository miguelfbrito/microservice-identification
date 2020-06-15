import re
import gensim
import logging
import pathlib
import numpy as np
import pyLDAvis.gensim

from WeightType import WeightType
from Clustering import Clustering
from nltk.tokenize import RegexpTokenizer
from stop_words import get_stop_words
from nltk.stem.porter import PorterStemmer
from gensim import corpora, models
from gensim.models import CoherenceModel
from StringUtils import StringUtils
from Settings import Settings


def apply_lda_to_classes(graph, classes, num_topics, pre_process=False):
    # classes {class_name : Class}
    docs = []
    # Remove loose sections of the graph
    if pre_process:
        classes_to_remove = []
        for cla in classes:
            if cla not in graph.nodes():
                classes_to_remove.append(cla)
        for cla in classes_to_remove:
            classes.pop(cla, None)

    # (class_name, BOW)
    docs = [(class_name, cla.get_merge_of_entities())
            for class_name, cla in classes.items()]

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

    with open(f"{Settings.DIRECTORY}/services_lda.txt", "w") as f:
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

    return clusters


"""Applies Latent Dirichlet Allocation to a list of documents
Parameters
----------
docs : [(doc_name, doc_content), ...]
num_topics : K topics refered in LDA
"""


def apply_lda_to_text(docs, num_topics):
    tokenizer = RegexpTokenizer(r'\w+')

    # create English stop words list
    en_stop = get_stop_words('en')

    # Create p_stemmer of class PorterStemmer
    p_stemmer = PorterStemmer()

    # with open(f"{Settings.DIRECTORY}/data/words/{Settings.PROJECT_NAME}_{Settings.ID}", 'w') as f:
    #     for d in docs:
    #         f.write(d + "\n")

    #     f.write("\n")

    # Clean text based on java stop words
    docs_content = []
    for doc in docs:
        directory = f"{Settings.DIRECTORY}/data/words/{Settings.PROJECT_NAME}/{doc[0]}"
        with open(directory, "w+") as f:
            f.write(f"{doc[0]}\n")
            f.write("Before processing:\n")
            f.write(f"{doc[1]}\n")

        doc_content = StringUtils.clear_java_words(doc[1])
        docs_content.append(doc_content)

        with open(directory, "a+") as f:
            f.write("\nAfter processing:\n")
            f.write(f"{doc_content}\n")

    # docs = [StringUtils.clear_java_words(doc) for doc in docs]
    # logging.info(docs)

    # compile sample documents into a list
    doc_set = docs_content

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
    dictionary.filter_extremes(no_below=2, no_above=0.8, keep_n=10000)

    # convert tokenized documents into a document-term matrix
    corpus = [dictionary.doc2bow(text) for text in texts]

    # generate LDA model
    # TODO : load a gensim model
    # https://radimrehurek.com/gensim/models/ldamodel.html
    # ldamodel = LdaModel.load()

    # model = gensim.models.ldamodel.LdaModel(
    # corpus, num_topics=num_topics, id2word=dictionary, passes=100)

    ldamodel = gensim.models.wrappers.LdaMallet(
        Settings.MALLET_PATH, corpus=corpus, num_topics=20, id2word=dictionary)
    # topics_per_doc = [ldamodel.get_document_topics(corp) for corp in corpus]

    topics_per_doc = []
    for c in ldamodel[corpus]:
        topics_per_doc.append(c)

    if Settings.LDA_PLOTTING:
        data = pyLDAvis.gensim.prepare(ldamodel, corpus, dictionary)
        pyLDAvis.show(data)

    # Compute Perplexity
    # a measure of how good the model is. lower the better.
    # Does not work for LDAMallet
    # print('\nPerplexity: ', ldamodel.log_perplexity(corpus))

    # Compute Coherence Score
    # ranging from 0 to 1, the higher the better.
    coherence_model_lda = CoherenceModel(
        model=ldamodel, texts=texts, dictionary=dictionary, coherence='c_v')
    coherence_lda = coherence_model_lda.get_coherence()
    print('\nCoherence Score: ', coherence_lda)

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

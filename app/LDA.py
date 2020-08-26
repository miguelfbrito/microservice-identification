import re
import operator
import time
import gensim
import logging
import pathlib
import operator
import numpy as np
import pyLDAvis.gensim
import matplotlib.pyplot as plt


from kneed import KneeLocator
from WeightType import WeightType
from Clustering import Clustering
from nltk.tokenize import RegexpTokenizer
from stop_words import get_stop_words
from nltk.stem.porter import PorterStemmer
from gensim import corpora, models
from gensim.models import CoherenceModel, nmf
from StringUtils import StringUtils
from Settings import Settings
from gensim.test.utils import datapath
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.decomposition import NMF


def apply_lda_to_classes(graph, classes, num_topics=0, pre_process=False):
    # classes {class_name : Class}

    num_topics = 5
    docs = []
    lda_result = []

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

    if Settings.K_TOPICS:
        num_topics = Settings.K_TOPICS
        _, corpus, dictionary = clear_documents(docs)
        model, _ = fit_lda(corpus, num_topics, dictionary)

        for c in model[corpus]:
            lda_result.append(c)

    else:
        lda_result, num_topics = find_best_lda(docs)

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


def clear_documents(docs):
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

        doc_content = StringUtils.clear_text(doc[1])
        docs_content.append(doc_content)

        with open(directory, "a+") as f:
            f.write("\nAfter processing:\n")
            f.write(f"{doc_content}\n")

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
    dictionary.filter_extremes(no_below=3, no_above=0.75, keep_n=1000)

    # convert tokenized documents into a document-term matrix
    corpus = [dictionary.doc2bow(text) for text in texts]

    return texts, corpus, dictionary


def compute_coherence_values(dictionary, corpus, texts, limit, start=4, step=3):
    coherence_values = []
    model_list = []
    num_topics_list = []

# Tf-idf
 #   docs = [' '.join(word) for word in texts]
 #   tfidf = TfidfVectorizer(max_df=0.9, stop_words='english')
    # accepts a list of documents of strings
 #   tf = tfidf.fit_transform(docs)

    for num_topics in range(start, limit, step):

        print(f"\n\nRunning LDA for {num_topics} topics")

        top_topics = [(0, 0)]
        runs = 0
        top_val = 0
        top_model = None
        temp_coherences = []

        while runs <= 3:
            print(f"New loop")

            lm, lda_model = fit_lda(corpus, num_topics, dictionary)

            if Settings.LDA_PLOTTING:
                data = pyLDAvis.gensim.prepare(lda_model, corpus, dictionary)
                pyLDAvis.show(data)

            if top_topics[0][1] >= top_val:
                top_model = lm

            coherencemodel = CoherenceModel(
                model=lm, texts=texts, dictionary=dictionary, coherence='c_v')

            print(f"Total coherence: {coherencemodel.get_coherence()}")
            temp_coherences.append(coherencemodel.get_coherence())

            runs += 1

            top_val = top_topics[0][1]

        # Same as before, duplicated above
        model_list.append(top_model)
        # coherencemodel = CoherenceModel(
        # model=top_model, texts=texts, dictionary=dictionary, coherence='c_v')
        # coherence_values.append(coherencemodel.get_coherence())
        coherence_values.append(max(temp_coherences))
        num_topics_list.append(num_topics)

    return model_list, coherence_values, num_topics_list


def fit_lda(corpus, num_topics, dictionary):
    lm = gensim.models.wrappers.LdaMallet(
        Settings.MALLET_PATH, corpus=corpus, num_topics=num_topics, id2word=dictionary)
    lda_model = gensim.models.wrappers.ldamallet.malletmodel2ldamodel(
        lm)

    return lm, lda_model


def find_best_lda(docs):
    len_docs = len(docs)

    step = 1
    if len_docs < 50:
        start = 4
        end = 12
    elif len_docs < 100:
        start = 4
        end = 16
        step = 1
    elif len_docs < 200:
        start = 10
        end = 25
        step = 3
    elif len_docs < 500:
        start = 12
        end = 30
        step = 3
    elif len_docs < 1000:
        start = 15
        end = 35
        step = 3
    else:
        start = 14
        end = 40
        step = 3

    texts, corpus, dictionary = clear_documents(docs)

    model_list, coherence_values, num_topics = compute_coherence_values(
        dictionary=dictionary, corpus=corpus, texts=texts, start=start, limit=end, step=step)

    x = range(start, end, step)
    for model, coherence, k in zip(model_list, coherence_values, num_topics):
        print(f"k {k} - coherence {coherence} lda_model {model}")

    # plt.plot(x, coherence_values)
    # plt.xlabel('number of topics')
    # plt.ylabel('topic coherence')
    # plt.show()

    S = 5
    best_topic = None
    while best_topic == None and S > 0:
        knee = KneeLocator(x, coherence_values, curve='concave',
                           direction='increasing', S=S)

        # Plot knee of coherence over number of topics
        # knee.plot_knee()
        # plt.xlabel('number of topics')
        # plt.ylabel('topic coherence')
        # plt.show()

        best_topic = knee.knee

        S -= 1
        print(f"Trying knee of S={S}")

    # In case the knee isn't found, select the max. Happens when the coherence values are very similar across topics
    if best_topic == None:
        coherence_list = list(coherence_values)
        best_topic = x[coherence_list.index(max(coherence_list))]
    Settings.K_TOPICS = best_topic
    print(
        f"The knee of topics/coherence is {best_topic}")

    lda_model = None
    for model, k_topic in zip(model_list, num_topics):
        if k_topic == best_topic:
            lda_model = model
            break
    topics_per_doc = []

    # topics_per_doc = [lda_model.get_document_topics(corp) for corp in corpus]

    for c in lda_model[corpus]:
        topics_per_doc.append(c)

    print(f"Topics per doc: {topics_per_doc}")
    print(f"Topics: {lda_model.show_topics()}")

    return topics_per_doc, best_topic


def set_weight_for_clustering(graph, class_visitors, topics_per_doc, k):

    class_topics = {z[0]: z[1]
                    for z in zip(class_visitors.keys(), topics_per_doc)}

    for src, dst in graph.edges():
        similarity = 0
        try:

            src_vector = topics_vector(class_topics[src], k)
            dst_vector = topics_vector(class_topics[dst], k)

            if len(src_vector) != 0 and len(dst_vector) != 0:
                similarity = Clustering.cosine_similarity(
                    src_vector, dst_vector)

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
            # In order to handle models with different sized vectors
            vector.append(0.000001)

    return np.array(vector)

from nltk.tokenize import RegexpTokenizer
from stop_words import get_stop_words
from nltk.stem.porter import PorterStemmer
from gensim import corpora, models
from FileUtils import FileUtils
import re
import gensim
import logging
import pyLDAvis.gensim


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


def apply_lda_to_text(docs):

    tokenizer = RegexpTokenizer(r'\w+')

    # create English stop words list
    en_stop = get_stop_words('en')

    # Create p_stemmer of class PorterStemmer
    p_stemmer = PorterStemmer()

    # Clean text based on java stop words
    docs = [FileUtils.clear_java_words(doc) for doc in docs]

    print(len(docs))

    logging.info(docs)

    # compile sample documents into a list
    doc_set = docs

    # list for tokenized documents in loop
    texts = []

    # loop through document list
    for i in doc_set:

        # clean and tokenize document string
        raw = i.lower()
        tokens = tokenizer.tokenize(raw)

        # remove stop words from tokens
        stopped_tokens = [i for i in tokens if not i in en_stop]

        # stem tokens
        stemmed_tokens = [p_stemmer.stem(i) for i in stopped_tokens]

        # add tokens to list
        texts.append(stemmed_tokens)

    # turn our tokenized documents into a id <-> term dictionary
    dictionary = corpora.Dictionary(texts)

    # convert tokenized documents into a document-term matrix
    corpus = [dictionary.doc2bow(text) for text in texts]

    # generate LDA model
    ldamodel = gensim.models.ldamodel.LdaModel(
        corpus, num_topics=5, id2word=dictionary, passes=20)

    # print(ldamodel.show_topics())
    # data = pyLDAvis.gensim.prepare(ldamodel, corpus, dictionary)
    # pyLDAvis.show(data)

    topics = ldamodel.show_topics(
        num_topics=1, num_words=6)

    pattern = r"(\d\.?\d*)\*\"(\w*)\""
    return re.findall(pattern, topics[0][1])

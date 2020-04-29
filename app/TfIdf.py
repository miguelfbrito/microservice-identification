# https://stackoverflow.com/questions/8897593/how-to-compute-the-similarity-between-two-text-documents
import nltk
import string
from sklearn.feature_extraction.text import TfidfVectorizer
from StringUtils import StringUtils


class TfIdf:
    def __init__(self):
        self.vectorizer = TfidfVectorizer(
            tokenizer=self.normalize, stop_words='english')
        self.stemmer = nltk.stem.porter.PorterStemmer()
        self.remove_punctuation_map = dict(
            (ord(char), None) for char in string.punctuation)

    def stem_tokens(self, tokens):
        return [self.stemmer.stem(item) for item in tokens]

    def normalize(self, text):
        return self.stem_tokens(nltk.word_tokenize(text.lower().translate(self.remove_punctuation_map)))

    def cosine_sim(self, text1, text2):
        tfidf = self.vectorizer.fit_transform([text1, text2])
        return ((tfidf * tfidf.T).A)[0, 1]

    def apply_tfidf_to_pair(self, source, target):
        source = StringUtils.clear_java_words(source)
        target = StringUtils.clear_java_words(target)

        return self.cosine_sim(source, target)

# https://stackoverflow.com/questions/8897593/how-to-compute-the-similarity-between-two-text-documents
import nltk
import string
from sklearn.feature_extraction.text import TfidfVectorizer
from FileUtils import FileUtils

stemmer = nltk.stem.porter.PorterStemmer()
remove_punctuation_map = dict((ord(char), None) for char in string.punctuation)


def stem_tokens(tokens):
    return [stemmer.stem(item) for item in tokens]


'''remove punctuation, lowercase, stem'''


def normalize(text):
    return stem_tokens(nltk.word_tokenize(text.lower().translate(remove_punctuation_map)))


vectorizer = TfidfVectorizer(tokenizer=normalize, stop_words='english')


def cosine_sim(text1, text2):
    tfidf = vectorizer.fit_transform([text1, text2])
    return ((tfidf * tfidf.T).A)[0, 1]


# print(cosine_sim('a little bird', 'a little bird chirps'))
# print(cosine_sim('a little bird', 'a big dog barks at a bird'))

directory = '/home/mbrito/git/thesis-web-applications/monoliths/simple-blog/'

files = FileUtils.search_java_files(directory)


index = 0
dict = {}
for f in files:
    print(f"File{index} {str(f)}")

    with open(f, 'r') as reader:
        dict[index] = {}
        dict[index]["file"] = f
        curr_text = reader.read()
        dict[index]["text"] = FileUtils.clear_java_words(curr_text)

    index += 1

# print(cosine_sim(dict.get(0)["text"], dict.get(1)["text"]))
# print(cosine_sim(dict.get(0)["text"], dict.get(2)["text"]))


def print_stuff(o1, o2):
    print(f"FileA {o1['file']}")
    print(f"FileB {o2['file']}")
    print(cosine_sim(o1["text"], o2["text"]))
    print("\n")


print_stuff(dict.get(0), dict.get(1))
print_stuff(dict.get(1), dict.get(3))  # 0.28595 pré-limpeza
print_stuff(dict.get(1), dict.get(2))  # 0.28595 pré-limpeza

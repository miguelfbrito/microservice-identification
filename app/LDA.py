from nltk.tokenize import RegexpTokenizer
from stop_words import get_stop_words
from nltk.stem.porter import PorterStemmer
from gensim import corpora, models
import gensim

tokenizer = RegexpTokenizer(r'\w+')

# create English stop words list
en_stop = get_stop_words('en')

# Create p_stemmer of class PorterStemmer
p_stemmer = PorterStemmer()

# create sample documents
doc_a = "Brocolli is good to eat. My brother likes to eat good brocolli, but not my mother."
doc_b = "My mother spends a lot of time driving my brother around to baseball practice."
doc_c = "Some health experts suggest that driving may cause increased tension and blood pressure."
doc_d = "I often feel pressure to perform well at school, but my mother never seems to drive my brother to do better."
doc_e = "Health professionals say that brocolli is good for your health."

doc_f = "com gaussic repository com gaussic model blog entity org springframework data jpa repository jpa repository org springframework data jpa repository modifying org springframework data jpa repository query org springframework data repository query param org springframework stereotype repository org springframework transaction annotation transactional java util date created by dzkan on 2016 3 18 repository blog repository jpa repository blog entity integer modifying transactional query update blog entity blog set blog title q title blog user by user id id q user id blog content q content blog pub date q pub date where blog id q id update blog param q title string title param q user id user id param q content string content param q pub date date pub date param q id id"

doc_g = "com gaussic controller com gaussic model user entity com gaussic repository user repository org springframework beans factory annotation autowired org springframework stereotype controller org springframework ui model map org springframework web bind annotation model attribute org springframework web bind annotation path variable org springframework web bind annotation request mapping org springframework web bind annotation request method java util list created by dzkan on 2016 3 8 controller main controller autowired user repository user repository request mapping value method request method g e t string index index request mapping value admin users method request method g e t string get users model map model map list user entity user list user repository find all model map add attribute user list user list admin users request mapping value admin users add method request method g e t string add user admin add user request mapping value admin users add p method request method p o s t string add user post model attribute user user entity user entity user repository save user entity system out println user entity get first name system out println user entity get last name user repository save and flush user entity redirect admin users request mapping value admin users show id method request method g e t string show user path variable id integer user id model map model map user entity user entity user repository find one user id model map add attribute user user entity admin user detail request mapping value admin users update id method request method g e t string update user path variable id integer user id model map model map user entity user entity user repository find one user id model map add attribute user user entity admin update user request mapping value admin users update p method request method p o s t string update user post model attribute user p user entity user user repository update user user get nickname user get first name user get last name user get password user get id user repository flush 刷新缓冲区 redirect admin users request mapping value admin users delete id method request method g e t string delete user path variable id integer user id user repository delete user id user repository flush redirect admin users"

doc_h = "com com model user entity com user repository org factory autowired org stereotype org ui model map org web bind model attribute org web bind path variable org web bind request mapping org web bind request method java util list created by dzkan on 2016 3 8 main controller autowired user repository user repository request mapping value request method g e t index index request mapping value admin users request method g e t get users model map model map list user entity user list user repository find all model map add attribute user list user list admin users request mapping value admin users add request method g e t add user admin add user request mapping value admin users add p request method p o s t add user post model attribute user user entity user entity user repository save user entity user entity get first name user entity get last name user repository save and flush user entity redirect admin users request mapping value admin users show id request method g e t show user path variable id user id model map model map user entity user entity user repository find one user id model map add attribute user user entity admin user detail request mapping value admin users update id request method g e t update user path variable id user id model map model map user entity user entity user repository find one user id model map add attribute user user entity admin update user request mapping value admin users update p request method p o s t update user post model attribute user p user entity user user repository update user user get nickname user get first name user get last name user get password user get id user repository flush 刷新缓冲区 redirect admin users request mapping value admin users delete id request method g e t delete user path variable id user id user repository delete user id user repository flush redirect admin users"

doc_i = "model user user org factory org stereotype org ui model org web bind model attribute org web bind path variable org web bind org web bind java util list created by dzkan on 2016 3 8 main user user g e t index index admin users g e t get users model model list user user list user find all model add attribute user list user list admin users admin users add g e t add user admin add user admin users add p p o s t add user post model attribute user user user user save user user get first name user get last name user save and flush user redirect admin users admin users show id g e t show user path variable id user id model model user user user find one user id model add attribute user user admin user detail admin users update id g e t update user path variable id user id model model user user user find one user id model add attribute user user admin update user admin users update p p o s t update user post model attribute user p user user user update user user get nickname user get first name user get last name user get password user get id user flush redirect admin users admin users delete id g e t delete user path variable id user id user delete user id user flush redirect admin users"

doc_l = "model javax persistence java util collection created by dzkan on 2016 3 8 table name user schema springdemo catalog user id nickname password first name last name collection blog blogs by id id column name id nullable get id id set id id id id basic column name nickname nullable length 45 get nickname nickname set nickname nickname nickname nickname basic column name password nullable length 45 get password password set password password password password basic column name first_name nullable length 45 get first name first name set first name first name first name first name basic column name last_name nullable length 45 get last name last name set last name last name last name last name override equals object o o o get o get user that user o id that id nickname nickname equals that nickname that nickname password password equals that password that password first name first name equals that first name that first name last name last name equals that last name that last name override hash code result id result 31 result nickname nickname hash code 0 result 31 result password password hash code 0 result 31 result first name first name hash code 0 result 31 result last name last name hash code 0 result one to many mapped by user by user id collection blog get blogs by id blogs by id set blogs by id collection blog blogs by id blogs by id blogs by id"


# compile sample documents into a list
doc_set = [doc_i]

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
    corpus, num_topics=1, id2word=dictionary, passes=20)

print(ldamodel.print_topics(num_topics=1, num_words=4))

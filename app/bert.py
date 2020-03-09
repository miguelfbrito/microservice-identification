#!/usr/bin/python3.7

from bert_serving.client import BertClient
from sklearn.metrics.pairwise import cosine_similarity

bc = BertClient()
vectors = bc.encode(['dog', 'cat', 'man'])
print(vectors[0])
print(len(vectors[0]))

#cos_lib = cosine_similarity(vectors[1,:],vectors[2,:]) #similarity between #cat and dog


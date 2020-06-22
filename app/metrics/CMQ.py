import re
import json
from itertools import combinations
from nltk.stem.porter import PorterStemmer

threshold = 0.0


def calculate(clusters, parsed_data):
    """
        clusters: dict of arrays { 0 : ['classA'], ...}
    """

    classes_to_ignore = set()
    class_terms = {}
    for classes in clusters.values():
        for classe in classes:
            classe_data = parsed_data[classe]
            for extend in classe_data['extendedTypes']:
                classes_to_ignore.add(extend)

            terms = []
            terms.append(classe.split('.')[-1])
            terms.extend(classe_data['variables'])
            for method in classe_data['methods'].values():
                terms.append(method['name'])
                terms.extend(method['parametersDataType'])
                terms.extend(method['returnDataType'])

            for method_invocation in classe_data['methodInvocations']:
                terms.append(method_invocation['methodName'])
                terms.append(method_invocation['scopeName'])
            cleared_terms = clear_text(' '.join(terms))
            class_terms[classe] = cleared_terms

    total_ccoh = 0
    for cluster in clusters.values():
        total_ccoh += ccoh(cluster, parsed_data,
                           classes_to_ignore, class_terms)
    print(f"Total ccoh {total_ccoh}")

    calc_ccop = ccop(clusters, parsed_data, classes_to_ignore, class_terms)
    print(f"Total ccop {calc_ccop}")

    N = len(clusters)
    cmq = 1 / N * total_ccoh - 1 / (N * (N - 1) / 2) * calc_ccop
    print(f"CMQ {cmq}")
    return cmq

# structural cohesiveness inside a service


def ccoh(cluster, parsed_classes, classes_to_ignore, class_terms):
    # An edge exists only if the intersection of the terms of both entities isn't empty

    edges = 0
    for idx1 in range(0, len(cluster) - 1):
        for idx2 in range(idx1 + 1, len(cluster)):

            terms_1 = set(class_terms[cluster[idx1]])
            terms_2 = set(class_terms[cluster[idx2]])

            intersection = terms_1.intersection(terms_2)
            union = terms_1.union(terms_2)
            if len(intersection) > len(union) * threshold:
                edges += 1

    print(
        f"edges {edges} , len cluster: {len(cluster)}, scoh: {edges / (len(cluster) * len(cluster))}")
    return edges / (len(cluster) * len(cluster))


def get_terms_of_clusters(cluster_id, clusters, class_terms):
    terms = set()
    for classe in clusters[cluster_id]:
        terms = terms.union(class_terms[classe])
    return terms


def ccop(clusters, parsed_data, classes_to_ignore, class_terms):
    edges = 0
    for idx1 in range(0, len(clusters) - 1):
        for idx2 in range(idx1 + 1, len(clusters)):

            terms_1 = set(get_terms_of_clusters(idx1, clusters, class_terms))
            terms_2 = set(get_terms_of_clusters(idx2, clusters, class_terms))

            union = terms_1.union(terms_2)
            intersection = terms_1.intersection(terms_2)
            if len(intersection) > len(union) * threshold:
                edges += 1

    print(
        f"ccop edges {edges}, len cluster: {len(clusters)}, ccop: {edges / (len(clusters) * len(clusters))}")
    return edges / (len(clusters) * len(clusters))


def clear_text(string):
    stopwords = {
        "abstract", "assert", "boolean",
        "break", "byte", "case", "catch", "char", "class", "const",
        "continue", "default", "do", "double", "else", "extends", "false",
        "final", "finally", "float", "for", "goto", "if", "implements",
        "import", "instanceof", "int", "interface", "long", "native",
        "new", "null", "package", "private", "protected", "public",
        "return", "short", "static", "strictfp", "super", "switch",
        "synchronized", "this", "throw", "throws", "transient", "true",
        "try", "void", "volatile", "while", "string", "int", "collection",
        "gaussic", "controller", "map", "request", "method", "integer", "system", "out", "println", "springframework",
        "com", "request", "mapping", "value", "autowired", "list", "hash",  "test", "id", "date", "spring", "mvc", "test", "mock", "except", "maven", "impl", "decimal", "serializable", "none", "set", "get", "object", "array", "mapper", "service", "entity", "repository", "annotation", "base", "model", "dao", "dto", "beans", "bean", "statement", "global", "view", "action", "http", "web", "jpa", "raysmond", "agilefant", "save", "insert", "delete", "update", "add", "remove", "search", "query", "factory", "context", "data", "form", "field", "router", "url", "database", "jdbc", "app",
        "connect", "util", "utils", "create"
    }

    result_words = []
    uncamel_words = re.sub(r'(?<!^)(?=[A-Z])', ' ', string).lower()
    words = re.split(r"\W+", uncamel_words)
    # with open('file', 'a+') as f:
    for word in words:
        if word.isalpha() and word.lower() not in stopwords and len(word) > 2:
            result_words.append(word)

    p_stemmer = PorterStemmer()
    stemmed_tokens = {p_stemmer.stem(rw) for rw in result_words}

    return stemmed_tokens


def string_to_dict_arrays(string):
    clusters = string.split(":")[1:]
    processed_clusters = {}
    for index, c in enumerate(clusters):
        processed_clusters[index] = []
        arr = []
        c = c.strip()
        # print(c)
        match = re.findall(r"'([a-zA-Z0-9._-]*)'", c)
        for m in match:
            processed_clusters[index].append(m)

    return processed_clusters


def calculateWrapper():
    directory = "/home/mbrito/git/thesis"
    # TODO: use Settings.DIRECTORY, didn't work as a script due to how the imports work
    projects_file = f"{directory}/projects.json"
    parsed_file = f"{directory}/data/output.json"

    clusters = []
    parsed_data = []

    with open(projects_file) as f:
        clusters = json.load(f)[0]['clusterString']

    with open(parsed_file) as f:
        parsed_data = json.load(f)

    clusters = string_to_dict_arrays(clusters)

    cmq = calculate(clusters, parsed_data)
    return cmq


if __name__ == "__main__":
    calculateWrapper()

import re
import json
from itertools import combinations


def calculate(clusters, parsed_data):
    """
        clusters: dict of arrays { 0 : ['classA'], ...}
    """

    N = sum(len(classes) for classes in clusters.values())
    print(f"Total Classes {N}")
    total_scoh = 0
    for cluster in clusters.values():
        total_scoh += scoh(cluster, parsed_data)

    total_scop = 0
    for src, dst in combinations(clusters.keys(), 2):
        calc_scop = scop(src, dst, clusters, parsed_data)

        total_scop += calc_scop
        print(f"Combination {src} {dst} -> {calc_scop}")
        pass

    print(f"Total scop {total_scop}")

    smq = 1 / N * total_scoh - 1 / (N * (N - 1) / 2) * total_scop
    print(f"SMQ {smq}")

    return smq

# structural cohesiveness inside a service


def scoh(cluster, parsed_classes):
    cluster = set(cluster)

    edges = 0
    for classe in cluster:
        try:
            classe_method_invocations = parsed_classes[classe]

            for method_invocation in classe_method_invocations['methodInvocations']:
                if method_invocation['targetClassName'] in cluster:
                    edges += 1
        except:
            print(f"[EXCEPTION KeyError] {classe} not found")

    print(
        f"edges {edges} , len cluster: {len(cluster)}, scoh: {edges / (len(cluster) * len(cluster))}")
    return edges / (len(cluster) * len(cluster))


# TODO : Remove after fixing the import from StringUtils
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

    return result_words


def process_text_terms(class_methods):
    names = parameters = returns = []
    for m in class_methods['name']:
        names.extend(clear_text(m))


def scop(cluster_i, cluster_j, clusters, parsed_data):
    classes_i = set(clusters[cluster_i])
    classes_j = set(clusters[cluster_j])

    total_edges = 0
    # Counts the number of edges between I and J
    for classe in classes_i:
        for method in parsed_data[classe]['methodInvocations']:
            if method['targetClassName'] in classes_j:
                print(f"Call from {classe} to {method['targetClassName']}")
                total_edges += 1

    # Same as above, but inverse order
    for classe in classes_j:
        for method in parsed_data[classe]['methodInvocations']:
            if method['targetClassName'] in classes_i:
                print(f"Call from {classe} to {method['targetClassName']}")
                total_edges += 1

    return total_edges / 2 * (len(classes_i) * len(classes_j))


def ccop(cluster_i, cluster_j, clusters, parsed_classes):
    class1_terms = parsed_classes[class1]['methodDeclarations']['methods']
    class2_terms = parsed_classes[class2]['methodDeclarations']['methods']

    process_text_terms(class1_terms)
    process_text_terms(class2_terms)


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
            print(m)
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

    smq = calculate(clusters, parsed_data)
    print(f"smq: {smq}")
    return smq


if __name__ == "__main__":
    calculateWrapper()

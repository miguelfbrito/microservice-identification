import re
import json
from itertools import combinations


def calculate(clusters, parsed_data):
    """
        clusters: dict of arrays { 0 : ['classA'], ...}
    """

    classes_to_ignore = set()
    for classes in clusters.values():
        for classe in classes:
            classe_data = parsed_data[classe]
            for extend in classe_data['extendedTypes']:
                classes_to_ignore.add(extend)

    total_scoh = 0
    for cluster in clusters.values():
        total_scoh += scoh(cluster, parsed_data, classes_to_ignore)

    total_scop = 0
    for src, dst in combinations(clusters.keys(), 2):
        calc_scop = scop(src, dst, clusters, parsed_data, classes_to_ignore)

        total_scop += calc_scop

    N = len(clusters.keys())
    smq = 1 / N * total_scoh - 1 / (N * (N - 1) / 2) * total_scop
    print(f"SMQ {smq}")

    return smq

# structural cohesiveness inside a service


def scoh(cluster, parsed_classes, classes_to_ignore):
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


def scop(cluster_i, cluster_j, clusters, parsed_data, classes_to_ignore):
    classes_i = set(clusters[cluster_i])
    classes_j = set(clusters[cluster_j])

    total_edges = 0
    # Counts the number of edges between I and J
    for classe in classes_i:
        if classe in classes_to_ignore:
            continue
        for method in parsed_data[classe]['methodInvocations']:
            if method['targetClassName'] in classes_j and method['targetClassName'] not in classes_to_ignore:
                total_edges += 1

    # Same as above, but inverse order
    for classe in classes_j:
        if classe in classes_to_ignore:
            continue
        for method in parsed_data[classe]['methodInvocations']:
            if method['targetClassName'] in classes_i and method['targetClassName'] not in classes_to_ignore:
                total_edges += 1

    # TODO : Review, formula states edges / 2 ( len i * len j) but FOSCI implementation doesn't include 2
    return total_edges / (len(classes_i) * len(classes_j))


def string_to_dict_arrays(string):
    clusters = string.split(":")[1:]
    processed_clusters = {}
    for index, c in enumerate(clusters):
        processed_clusters[index] = []
        arr = []
        c = c.strip()
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

    smq = calculate(clusters, parsed_data)
    return smq


if __name__ == "__main__":
    calculateWrapper()

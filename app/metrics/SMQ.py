import re
import json
from Settings import Settings
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
    total_scoh = total_scoh / N
    total_scop = total_scop / (N * (N - 1) / 2)
    smq = total_scoh - total_scop

    return smq, total_scoh, total_scop

# structural cohesiveness inside a service


def scoh(cluster, parsed_classes, classes_to_ignore):
    cluster = set(cluster)

    edges = 0
    max_edges = 0
    for src, dst in combinations(cluster, 2):
        max_edges += 2  # bidirectional
        try:
            src_invocations = {method['targetClassName']
                               for method in parsed_classes[src]['methodInvocations']}
            dst_invocations = {method['targetClassName']
                               for method in parsed_classes[dst]['methodInvocations']}

            src_invocations = src_invocations | set(
                parsed_classes[src]['dependencies'])
            dst_invocations = dst_invocations | set(
                parsed_classes[dst]['dependencies'])

            # Check both directions
            if src in dst_invocations:
                edges += 1
            if dst in src_invocations:
                edges += 1

        except KeyError:
            print(f"[EXCEPTION KeyError] {src} or {dst} not found")

    if max_edges == 0:
        return 0
    print(
        f"SCOH: edges {edges} , len cluster: {len(cluster)}, scoh: {edges / (max_edges)}")
    return edges / (max_edges)


def scop(cluster_i, cluster_j, clusters, parsed_data, classes_to_ignore):
    classes_i = set(clusters[cluster_i])
    classes_j = set(clusters[cluster_j])

    # print(f"Classe_i {cluster_i} {classes_i}")
    # print(f"Classe_j {cluster_j} {classes_j}")

    total_edges = 0
    # Counts the number of edges between I and J
    for classe in classes_i:
        for method in parsed_data[classe]['methodInvocations']:
            if method['targetClassName'] in classes_j:
                total_edges += 1

    # Same as above, but inverse order
    for classe in classes_j:
        for method in parsed_data[classe]['methodInvocations']:
            if method['targetClassName'] in classes_i:
                total_edges += 1

    # print(
    #     f"SCOP: edges {total_edges},  scop: {total_edges / (2 * (len(classes_i) * len(classes_j)))}")
    return total_edges / (2 * (len(classes_i) * len(classes_j)))


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
    projects_file = f"{Settings.DIRECTORY}/projects.json"
    parsed_file = f"{Settings.DIRECTORY}/data/output.json"

    clusters = []
    parsed_data = []

    with open(projects_file) as f:
        clusters = json.load(f)[0]['clusterString']

    with open(parsed_file) as f:
        parsed_data = json.load(f)

    clusters = string_to_dict_arrays(clusters)

    smq, scoh, scop = calculate(clusters, parsed_data)

    print(f"Final SMQ: {smq}")
    print(f"Final scoh: {scoh}")
    print(f"Final scop: {scop}")
    return smq, scoh, scop


if __name__ == "__main__":
    calculateWrapper()

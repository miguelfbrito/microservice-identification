from StringUtils import StringUtils
from Graph import Graph
from operator import itemgetter
from TfIdf import TfIdf
from WeightType import WeightType
from Clustering import Clustering
from Visitors.ClassVisitor import ClassVisitor
from ProcessResultsOutput import ProcessResultsOutput

import re
import math
import argparse
import community
import collections
import LDA as lda
import numpy as np

import logging
import javalang
import networkx as nx
import matplotlib.pyplot as plt

from random import random
from random import randint
from itertools import cycle
from sklearn.mixture import GaussianMixture
from sklearn.cluster import DBSCAN, FeatureAgglomeration, AffinityPropagation


def read_files(files):
    read_files = {}
    for f in files:

        with open(f, 'r') as reader:
            matches = re.search(r"\/(?P<class_name>\w*)\.java", str(f))
            if matches:
                class_name = matches.groupdict()['class_name']
                read_files[class_name] = {}
                read_files[class_name]["fullpath"] = str(f)
                read_files[class_name]["text"] = reader.read()

    return read_files


def parse_files_to_ast(read_files):
    class_visitors = {}

    for file_name, values in read_files.items():
        logging.info(f"Parsing file {values['fullpath']}")
        try:
            tree = javalang.parse.parse(values["text"])
        except javalang.parser.JavaSyntaxError:
            logging.error(f"Failed to parse file: {values['fullpath']}")

        visitor = ClassVisitor()
        visitor.extract_comments(values["text"])

        for _, node in tree:
            visitor.visit(node)

        class_visitors[visitor.get_class_name()] = visitor

    return class_visitors


def calculate_absolute_weights(graph, weight_type=WeightType.TF_IDF):

    # Drawing of label explained here - https://stackoverflow.com/questions/31575634/problems-printing-weight-in-a-networkx-graph
    total_structural_weight = 0
    structural_weight_distribution = {}
    for src, dst in graph.edges():

        edge_data = graph[src][dst]

        # If the dependency is of type EXTENDS, IMPLEMENTS or STATIC (less common than NORMAL)
        primary_types = {'EXTENDS', 'IMPLEMENTS', 'STATIC'}
        # secondary_types = {'STATIC'}
        total_structural_weight += edge_data[str(WeightType.STRUCTURAL)]
        structural_weight_distribution[edge_data[str(WeightType.STRUCTURAL)]] = structural_weight_distribution.get(
            edge_data[str(WeightType.STRUCTURAL)], 0) + 1

        # TODO : consider just removing the edge and adding it after clustering
        if edge_data["dependency_type"] in primary_types:
            edge_data[str(WeightType.ABSOLUTE)] = 0.9
        else:
            edge_data[str(WeightType.ABSOLUTE)] = round(
                edge_data[str(weight_type)], 2)  # + edge_data[str(WeightType.STRUCTURAL)], 2)

        # print(f"{src} -> {dst} : {edge_data}")

    print(f"Total structural weight: {total_structural_weight}")
    print(f"Structural weight distribution: {structural_weight_distribution}")


def draw_graph(graph, weight_type=WeightType.ABSOLUTE):

    # for src, dst in graph.edges():
    #     edge_data = graph.get_edge_data(src, dst)
    #     if edge_data and edge_data[weight_type] < 0.3:
    #         graph[src][dst][weight_type] = 0

    # Edges can't have a weight of 0, resulting in a divide by 0 error on the kamada_kaway_kayout calculation
    # for u, v in graph.edges():
    #     data = graph.get_edge_data(u, v)
    #     if data:
    #         if data[weight_type] == 0:
    #             graph[u][v][weight_type] = 0.001
    # pos = nx.kamada_kawai_layout(graph, weight=weight_type)

    pos = nx.spring_layout(graph, weight=weight_type)

    # Drawing of labels explained here - https://stackoverflow.com/questions/31575634/problems-printing-weight-in-a-networkx-graph
    new_labels = dict(map(lambda x: ((x[0], x[1]),  str(
        x[2][str(weight_type)]) if x[2][str(weight_type)] > 0 else ""), graph.edges(data=True)))

    nx.draw_networkx(graph, pos=pos, node_size=500, alpha=0.8,
                     font_size=10)
    nx.draw_networkx_edge_labels(
        graph, pos, edge_labels=new_labels, font_size=7, alpha=0.9)
    # nx.draw_networkx_edges(graph, pos, width=0, arrows=False)

    plt.show()


def apply_tfidf_to_connections(graph, class_visitors):

    edges = graph.edges()

    tf_idf = TfIdf()
    for src, dst in edges:
        source = class_visitors[src].get_merge_of_strings()
        destination = class_visitors[dst].get_merge_of_strings()

        similarity = round(tf_idf.apply_tfidf_to_pair(source, destination), 2)
        logging.info(f"{similarity} {src} - {dst}")

        graph[src][dst][str(WeightType.TF_IDF)] = similarity


def test_clustering_algorithms(graph):
    print("\nGirvan Method 1")
    Clustering.girvan_newman(graph)

    print("\nGirvan Method 2")
    Clustering.girvan_newman_weight(graph)

    print("\nKernighan Lin Bisection")
    Clustering.kernighan_lin_bisection(graph)

    print("\nGreedy_modularity_communities")
    Clustering.greedy_modularity_communities(graph)

    print("\nLabel_propagation_communities")
    Clustering.label_propagation_communities(graph)


def prepare_matrix(graph, weight_type=WeightType.ABSOLUTE):
    # https://stackoverflow.com/questions/49064611/how-to-find-different-groups-in-networkx-using-python
    # X = nx.to_numpy_matrix(graph, weight=WEIGHT)

    g = graph.to_undirected()
    nn = len(g.nodes)

    mat = np.empty((nn, nn), dtype=float)
    mat.fill(-50)  # -50?
    np.fill_diagonal(mat, -0.0)

    node_to_int = {node: index for index, node in enumerate(g.nodes)}

    for u, v in g.edges:
        weight = (g.get_edge_data(u, v)[weight_type])
        u = node_to_int[u]
        v = node_to_int[v]

        mat[u, v] = -50 * (1 - weight)

    np.median(mat)

    return mat, node_to_int


# TODO : Investigate other methods of clustering
# https://scikit-learn.org/stable/modules/classes.html#module-sklearn.cluster
def community_detection_by_affinity(graph, weight_type=WeightType.ABSOLUTE):

    # Has an high impact on Girvan Newman clustering
    graph = nx.algorithms.tree.mst.maximum_spanning_tree(
        graph.to_undirected())

    mat, node_to_int = prepare_matrix(graph)

    af = AffinityPropagation(preference=-50)
    labels = af.fit_predict(mat)

    inv_node_to_int = {v: k for k, v in node_to_int.items()}

    clusters = {}
    for index, lab in enumerate(labels):
        class_name = inv_node_to_int[index]
        if lab not in clusters:
            clusters[lab] = []
        clusters[lab].append(class_name)

    print(f"\nClusters: {clusters}")
    print(f"Total Clusters: {len(clusters)}")

    pos = nx.spring_layout(graph)
    nx.draw_networkx(graph, pos=pos, edgelist=[], node_color=labels, with_labels=True,
                     node_size=250, font_size=8)
    plt.show()

    return clusters
    # y_pos = np.arange(len(clusters.items()))
    # plt.bar(y_pos, cluster_distribution)
    # plt.ylabel("Cluster Size")
    # plt.xlabel("Cluster Index")


def visitors_to_qualified_name(visitors):
    qualified_visitors = {}
    for class_name, visitor in visitors.items():
        qualified_name = f"{visitor.get_package_name()}.{class_name}"
        qualified_visitors[qualified_name] = visitor

    return qualified_visitors


def identify_clusters_in_project(project):

    project_name = project[0]
    num_topics = project[1]
    directory = '/home/mbrito/git/thesis-web-applications/monoliths/' + project_name

    files = StringUtils.search_java_files(directory)
    files = read_files(files)

    graph = nx.DiGraph()
    class_visitors = parse_files_to_ast(files)
    graph = Graph.create_dependencies(class_visitors, graph)

    qualified_visitors = visitors_to_qualified_name(class_visitors)
    Graph.clean_irrelevant_dependencies(qualified_visitors, graph)

    # Method 1. TF-IDF
    # apply_tfidf_to_connections(graph, qualified_visitors)

    # Method 2. LDA
    # TODO : think about if the pre_processing should be done or not
    lda.apply_lda_to_classes(graph, qualified_visitors, num_topics)

    calculate_absolute_weights(graph, weight_type=WeightType.LDA)
    graph = Clustering.pre_process(
        graph, remove_weak_edges=False, remove_disconnected_sections=True)

    return Clustering.community_detection_louvain(graph)
    # return clusters


def main():
    logging.basicConfig(filename='logs.log', filemode="w", level=logging.INFO,
                        format="%(asctime)s:%(levelname)s: %(message)s")
    # Enables printing of logs to stdout as well
    # logging.getLogger().addHandler(logging.StreamHandler())

    parser = argparse.ArgumentParser()
    parser.add_argument("--metrics", "-m",
                        help="Parse, cluster and execute metrics for a given project name (relative path to set root path)")
    parser.add_argument("--k_topics", "-k",
                        help="Number of topics for given project")
    parser.add_argument("--metrics-condensed", "-mc",
                        help="Parse, cluster and execute metrics for a subset of projects", action="store_true")
    parser.add_argument("--metrics-full", "-mf",
                        help="Parse, cluster, and execute metrics for all defined projects", action="store_true")
    args = parser.parse_args()

    if args.metrics:
        result = ProcessResultsOutput()
        project = (args.metrics, int(args.k_topics))
        clusters = identify_clusters_in_project(project)
        result.add_project(project[0], str(clusters))
        result.dump_to_json_file()
        result.run_java_metrics()

    if args.metrics_condensed:
        projects = [('spring-blog', 4), ('jpetstore', 4),
                    ('monomusiccorp', 8), ('spring-petclinic', 3)]

        results = ProcessResultsOutput()
        for project in projects:
            print(f"\n\nStarting project {project[0]}, {project[1]} topics")
            clusters = identify_clusters_in_project(project)
            clusters = [cluster for cluster in clusters.values()]
            results.add_project(project[0], str(clusters))
        results.dump_to_json_file()
        results.run_java_metrics()

    if args.metrics_full:
        projects = [('spring-blog', 4), ('jpetstore', 4),
                    ('monomusiccorp', 8), ('spring-petclinic', 3), ('jforum', 13), ('agilefant', 14)]
        results = ProcessResultsOutput()
        for project in projects:
            print(f"\n\nStarting project {project}")
            clusters = identify_clusters_in_project(project)
            clusters = [cluster for cluster in clusters.values()]
            results.add_project(project[0], str(clusters))
        results.dump_to_json_file()
        results.run_java_metrics()


main()

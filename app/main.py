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


def calculate_absolute_weights(graph):

    # Drawing of label explained here - https://stackoverflow.com/questions/31575634/problems-printing-weight-in-a-networkx-graph
    for src, dst in graph.edges():
        edge_data = graph[src][dst]

        # If the dependency is of type EXTENDS, IMPLEMENTS or STATIC (less common than NORMAL)
        if edge_data["dependency_type"] != "NORMAL":
            edge_data[WeightType.ABSOLUTE] = .8
        else:
            edge_data[WeightType.ABSOLUTE] = edge_data[WeightType.TF_IDF]


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
        x[2][weight_type]) if x[2][weight_type] > 0 else ""), graph.edges(data=True)))

    nx.draw_networkx(graph, pos=pos, node_size=500, alpha=0.8,
                     font_size=10)
    nx.draw_networkx_edge_labels(
        graph, pos, edge_labels=new_labels, font_size=7, alpha=0.9)
    # nx.draw_networkx_edges(graph, pos, width=0, arrows=False)

    plt.show()


def apply_lda_to_classes(class_visitors, all=True):

    # Apply lda all classes
    if all:
        docs = ([[cla.get_merge_of_strings()] for cla in class_visitors])
        lda_result = lda.apply_lda_to_text(docs)

    else:
        # Apply lda individually
        for cla in class_visitors:
            logging.info(f"Applying LDA to {cla.get_class_name()}")

            try:
                lda_result = lda.apply_lda_to_text(cla.get_merge_of_strings())

                # For now we only care about documents evaluated individually, hence the 0.
                cla.set_lda(lda_result)
                logging.info(cla.get_lda())

            except ValueError:
                logging.warning(
                    "Failed to process a file. It probably contains annotations that the parser is not prepared to handle (eg. @interface)")


def get_src_dst_visitor(src, dst, class_visitors):
    # TODO: Optimize by changing this list to a dict and doing O(1) accesses
    src_visitor = None
    dst_visitor = None

    for visitor in class_visitors:
        class_name = visitor.get_class_name()
        if src == class_name:
            src_visitor = visitor

        if dst == class_name:
            dst_visitor = visitor

    return src_visitor, dst_visitor


def apply_tfidf_to_connections(graph, class_visitors):

    edges = graph.edges()

    tf_idf = TfIdf()
    for src, dst in edges:
        src_text = class_visitors[src].get_merge_of_strings()
        dst_text = class_visitors[dst].get_merge_of_strings()

        similarity = round(tf_idf.apply_tfidf_to_pair(src_text, dst_text), 2)
        logging.info(f"{similarity} {src} - {dst}")

        graph[src][dst][WeightType.TF_IDF] = similarity


def set_edge_weight_by_identified_topics(graph, class_visitors):

    for src, dst in graph.edges():
        src_visitor, dst_visitor = get_src_dst_visitor(
            src, dst, class_visitors)

        # print(
        #     f"{src_visitor.get_class_name()} - {dst_visitor.get_class_name()}")
        best_match = lda.best_match_between_topics(
            src_visitor.get_lda(), dst_visitor.get_lda())
        logging.info(f"Best match {best_match}")

        graph[src][dst][WeightType.LDA] = round(
            best_match[0], 2) if best_match else 0


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


def prepare_matrix(graph):
    # https://stackoverflow.com/questions/49064611/how-to-find-different-groups-in-networkx-using-python
    # X = nx.to_numpy_matrix(graph, weight=WEIGHT)

    g = graph.to_undirected()
    nn = len(g.nodes)

    mat = np.empty((nn, nn), dtype=float)
    mat.fill(-50)  # -50?
    np.fill_diagonal(mat, -0.0)

    node_to_int = {node: index for index, node in enumerate(g.nodes)}

    for u, v in g.edges:
        weight = (g.get_edge_data(u, v)['weight'])
        u = node_to_int[u]
        v = node_to_int[v]

        mat[u, v] = -50 * (1 - weight)

    np.median(mat)

    return mat, node_to_int


# TODO : Investigate other methods of clustering
# https://scikit-learn.org/stable/modules/classes.html#module-sklearn.cluster
def community_detection_by_affinity(graph):

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


def community_detection_louvain(g, weight_type=WeightType.ABSOLUTE):
    g = g.to_undirected()
    partition = community.best_partition(g, weight=str(WeightType.ABSOLUTE))
    values = [partition.get(node) for node in g.nodes()]

    for node in g.nodes:
        node
    counter = collections.Counter(values)

    nodes = list(g.nodes)
    clusters = {}
    for index, val in enumerate(values):
        if val not in clusters:
            clusters[val] = []

        clusters[val].append(nodes[index])

    print(f"Total Clusters: {len(clusters)}")
    print(
        f"Total Clusters len>2: {len([cluster for cluster in clusters if len(clusters[cluster]) > 2])}")

    # Relabel nodes from qualified name (package+classname) for better visibility
    h = g.copy()
    mappings = {}
    for node in h.nodes():
        mappings[node] = re.search(r'\.(\w*)$', node)[1]
    h = nx.relabel_nodes(h, mappings)

    # Drawing of labels explained here - https://stackoverflow.com/questions/31575634/problems-printing-weight-in-a-networkx-graph

    sp = nx.spring_layout(h, weight=weight_type)
    nx.draw_networkx(h, pos=sp, with_labels=True,
                     node_size=250, font_size=6, node_color=values, label_color="red")

    # Label weight drawing
    # new_labels = dict(map(lambda x: ((x[0], x[1]),  str(
    #     x[2][weight_type]) if x[2][weight_type] > 0 else ""), h.edges(data=True)))
    # nx.draw_networkx_edge_labels(
    #     h, sp, edge_labels=new_labels, font_size=7, alpha=0.9)
    # plt.show()

    cluster_distribution = [len(cluster) for cluster in clusters.values()]
    print(f"Cluster distribution: {cluster_distribution}")

    return clusters

    # y_pos = np.arange(len(clusters.items()))
    # plt.bar(y_pos, cluster_distribution)
    # plt.ylabel("Cluster Size")
    # plt.xlabel("Cluster Index")
    # plt.show()


def visitors_to_qualified_name(visitors):
    qualified_visitors = {}
    for class_name, visitor in visitors.items():
        qualified_name = f"{visitor.get_package_name()}.{class_name}"
        qualified_visitors[qualified_name] = visitor

    return qualified_visitors


def identify_clusters_in_project(project_name):
    directory = '/home/mbrito/git/thesis-web-applications/monoliths/' + project_name

    files = StringUtils.search_java_files(directory)
    files = read_files(files)

    graph = nx.DiGraph()
    class_visitors = parse_files_to_ast(files)
    graph = Graph.create_dependencies(class_visitors, graph)

    qualified_visitors = visitors_to_qualified_name(class_visitors)
    # Graph.clean_irrelevant_dependencies(qualified_visitors, graph)

    # Method 1. TF-IDF
    apply_tfidf_to_connections(graph, qualified_visitors)

    # Method 2. LDA
    # apply_lda_to_classes(class_visitors)
    # set_edge_weight_by_identified_topics(graph, class_visitors)

    calculate_absolute_weights(graph)
    return community_detection_louvain(graph)

    # test_clustering_algorithms(graph.to_undirected())


def main():
    logging.basicConfig(filename='logs.log', filemode="w", level=logging.INFO,
                        format="%(asctime)s:%(levelname)s: %(message)s")
    # Enables printing of logs to stdout as well
    # logging.getLogger().addHandler(logging.StreamHandler())

    # project_name = 'simple-blog'
    # project_name = 'test'
    # project_name = 'spring-blog'
    # project_name = 'spring-petclinic'
    # project_name = 'spring-boot-admin/spring-boot-admin-server'
    # project_name = 'broadleaf-commerce/core/broadleaf-framework'  # 727 classes
    # project_name = 'monomusiccorp'
    # project_name = 'jpetstore'

    projects = ['spring-blog', 'jpetstore',
                'monomusiccorp', 'spring-petclinic']

    results = ProcessResultsOutput()
    for project in projects:
        print(f"\n\nStarting project {project}")
        clusters = identify_clusters_in_project(project)
        clusters = [cluster for cluster in clusters.values()]
        results.add_project(project, str(clusters))

    results.dump_to_json_file()
    results.run_java_metrics()

    # Has an high impact on Girvan Newman clustering
    # graph = nx.algorithms.tree.mst.maximum_spanning_tree(
    #     graph.to_undirected(), weight=WeightType.ABSOLUTE)
    # community_detection_by_affinity(graph)
main()

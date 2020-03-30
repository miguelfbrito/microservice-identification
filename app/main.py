from FileUtils import FileUtils
from ClassVisitor import ClassVisitor
from operator import itemgetter
from TfIdf import TfIdf
from Clustering import Clustering

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
from random import randint
from random import random
from sklearn.mixture import GaussianMixture
from sklearn.cluster import AffinityPropagation
from sklearn.cluster import SpectralClustering


WEIGHT_LDA = "weight_lda"
WEIGHT_TF_IDF = "weight_tf_idf"
WEIGHT_STRUCTURAL = "weight_structural"
WEIGHT_ABSOLUTE = "weight"


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


def create_graph_dependencies(visitors, graph):
    """ Based on the AST counts the number of connections and sets them as weight on a graph """

    for visitor in visitors:
        for dependency in visitor.get_dependencies():

            dependency_name = dependency[0]
            dependency_type = dependency[1]

            # In order to remove duplicates (when a class references itself)
            if visitor.get_class_name() == dependency_name:
                continue

            edge_data = graph.get_edge_data(
                visitor.get_class_name(), dependency_name)

            if edge_data:
                graph[visitor.get_class_name(
                )][dependency_name][WEIGHT_STRUCTURAL] = edge_data[WEIGHT_STRUCTURAL] + 1
                # We will not update the type because the first time we set it, it will be set for
                # EXTENDS or IMPLEMENTs which have higher priority
            else:
                graph.add_edge(visitor.get_class_name(),
                               dependency_name, weight_structural=1, dependency_type=dependency_type)

    return graph


def clean_irrelevant_dependencies(visitors, graph):
    classes = [visitor.get_class_name() for visitor in visitors]
    nodes = list(graph.nodes)

    # Iterate over nodes and remove the ones not present in classes
    for node in nodes:
        try:
            if node not in classes:
                graph.remove_node(node)
        except nx.exception.NetworkXError:
            print("Node not found while removing")


def parse_files_to_ast(read_files):
    class_visitors = []
    graph = nx.DiGraph()

    for values in read_files.values():
        logging.info(f"Parsing file ${values['fullpath']}")
        try:
            tree = javalang.parse.parse(values["text"])
        except javalang.parser.JavaSyntaxError:
            logging.error(f"Failed to parse file: {values['fullpath']}")

        visitor = ClassVisitor()
        visitor.extract_comments(values["text"])

        for _, node in tree:
            visitor.visit(node)

        class_visitors.append(visitor)
    return class_visitors, graph


def calculate_absolute_weights(graph):

    # Drawing of label explained here - https://stackoverflow.com/questions/31575634/problems-printing-weight-in-a-networkx-graph
    for src, dst in graph.edges():
        edge_data = graph[src][dst]

        # If the dependency is of type EXTENDS or IMPLEMENTS (less common than NORMAL)
        if edge_data["dependency_type"] != "NORMAL":
            edge_data[WEIGHT_ABSOLUTE] = 1
        else:
            edge_data[WEIGHT_ABSOLUTE] = edge_data[WEIGHT_TF_IDF]


def draw_graph(graph, weight_type=WEIGHT_ABSOLUTE):

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
            logging.info(f"Applying LDA to ${cla.get_class_name()}")

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
        src_text = ""
        dst_text = ""

        src_text, dst_text = get_src_dst_visitor(src, dst, class_visitors)
        src_text = src_text.get_merge_of_strings()
        dst_text = dst_text.get_merge_of_strings()

        similarity = round(tf_idf.apply_tfidf_to_pair(src_text, dst_text), 2)
        print(f"${similarity} ${src} - ${dst}")

        graph[src][dst][WEIGHT_TF_IDF] = similarity


def set_edge_weight_by_identified_topics(graph, class_visitors):

    for src, dst in graph.edges():
        src_visitor, dst_visitor = get_src_dst_visitor(
            src, dst, class_visitors)

        # print(
        #     f"{src_visitor.get_class_name()} - {dst_visitor.get_class_name()}")
        best_match = lda.best_match_between_topics(
            src_visitor.get_lda(), dst_visitor.get_lda())
        logging.info(f"Best match {best_match}")

        graph[src][dst][WEIGHT_LDA] = round(
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


def community_detection_by_affinity(graph):

    X = nx.to_numpy_matrix(graph, weight=WEIGHT_ABSOLUTE)
    print(X)

    # https://stackoverflow.com/questions/49064611/how-to-find-different-groups-in-networkx-using-python
    g = graph.to_undirected()
    nn = len(g.nodes)

    mat = np.empty((nn, nn), dtype=float)
    mat.fill(-100.0)
    np.fill_diagonal(mat, -0.0)

    node_to_int = {node: index for index, node in enumerate(g.nodes)}
    print(node_to_int)

   # Ignoring jaccard coefficient for now
   # preds = nx.jaccard_coefficient(g, g.edges)
    for u, v in g.edges:

        weight = (g.get_edge_data(u, v)['weight'])
        u = node_to_int[u]
        v = node_to_int[v]

        mat[u, v] = weight

    np.median(mat)
    af = AffinityPropagation(preference=-100, affinity="precomputed")
    lab = af.fit_predict(mat)
    len(np.unique(lab))

    partition = community.best_partition(g, weight='weight')
    values = [partition.get(node) for node in g.nodes()]
    counter = collections.Counter(values)
    print(counter)

    sp = nx.spring_layout(g)
    nx.draw_networkx(g, pos=sp, edgelist=[], with_labels=True,
                     node_size=250, font_size=8, node_color=values)
    plt.show()


def main():

    logging.basicConfig(filename='logs.log', filemode="w", level=logging.INFO,
                        format="%(asctime)s:%(levelname)s: %(message)s")
    # Enables printing of logs to stdout as well
    # logging.getLogger().addHandler(logging.StreamHandler())

    # project_name = 'test'
    # project_name = 'simple-blog'
    project_name = 'spring-petclinic'
    # project_name = 'spring-boot-admin/spring-boot-admin-server'
    # project_name = 'BroadleafCommerce/core/broadleaf-framework'  # 727 classes
    # project_name = 'monomusiccorp'
    directory = '/home/mbrito/git/thesis-web-applications/monoliths/' + project_name
    # directory_test = '/home/mbrito/git/thesis/app'
    files = FileUtils.search_java_files(directory)
    files = read_files(files)

    class_visitors, graph = parse_files_to_ast(files)
    graph = create_graph_dependencies(class_visitors, graph)
    clean_irrelevant_dependencies(class_visitors, graph)

    # Method 1. TF-IDF
    apply_tfidf_to_connections(graph, class_visitors)

    # Method 2. LDA
    # apply_lda_to_classes(class_visitors)
    # set_edge_weight_by_identified_topics(graph, class_visitors)

    calculate_absolute_weights(graph)

    # Has an high impact on Girvan Newman clustering
    # graph = nx.algorithms.tree.mst.maximum_spanning_tree(
    #     graph.to_undirected(), weight=WEIGHT_ABSOLUTE)

    community_detection_by_affinity(graph)


main()

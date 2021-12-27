from sklearn.cluster import DBSCAN, FeatureAgglomeration, AffinityPropagation
from sklearn.mixture import GaussianMixture
from datetime import datetime
from itertools import cycle
from random import randint
from random import random
from kneed import KneeLocator
import matplotlib.pyplot as plt
import Utils as utils
import networkx as nx
import javalang
import logging
import numpy as np
import LDA as lda
import collections
import community
import argparse
import json
import math
import re
import os
import PostProcessing
import time


from metrics.Metrics import Metrics
from metrics.MetricExecutor import MetricExecutor
from Utils import normalize
from StringUtils import StringUtils
from Graph import Graph
from operator import itemgetter
from TfIdf import TfIdf
from WeightType import WeightType
from Clustering import Clustering
from visitors.ClassVisitor import ClassVisitor
from entities.Method import Method
from entities.Class import Class
from entities.Service import Service
from Settings import Settings


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
        logging.info(f"Pagraphsing file {values['fullpath']}")
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


def calculate_absolute_weights(graph, classes, weight_type=WeightType.TF_IDF):

    graph = Graph.normalize_values(graph, str(WeightType.STRUCTURAL))

    # Drawing of label explained here - https://stackoverflow.com/questions/31575634/problems-printing-weight-in-a-networkx-graph
    for src, dst in graph.edges():

        edge_data = graph[src][dst]

        # If the dependency is of type EXTENDS, IMPLEMENTS or STATIC (less common than NORMAL)
        primary_types = {'EXTENDS', 'IMPLEMENTS', 'STATIC'}
        # secondary_types = {'STATIC'}

        # TODO : consider just removing the edge and adding it after clustering
        try:
            if edge_data["dependency_type"] in primary_types:
                edge_data[str(WeightType.ABSOLUTE)] = float(1)
            else:
                # edge_data[str(WeightType.ABSOLUTE)
                #           ] = float(edge_data[str(weight_type)])

                method_call_weight = 0
                if src in classes:
                    classe = classes[src]
                    calls_to_dst = 0
                    for method_call in classe.get_method_invocations():
                        target_class = method_call['targetClassName']
                        if dst == target_class:
                            calls_to_dst += 1

                    if len(classe.get_method_invocations()) > 0:
                        method_call_weight = calls_to_dst / \
                            len(classe.get_method_invocations())

                # TODO : REVIEW AND REMOVE, testing purposes only
                method_call_weight = 0
                edge_data[str(WeightType.ABSOLUTE)
                          ] = max(float(edge_data[str(weight_type)]), method_call_weight)  # method_call_weight
        except KeyError as e:
            # TODO : review why does this only happens on a specific case
            edge_data[str(WeightType.ABSOLUTE)] = 0
            print(f"KEY ERROR {e} {src} {dst}")


def apply_tfidf_to_connections(graph, class_visitors):

    edges = graph.edges()

    tf_idf = TfIdf()
    for src, dst in edges:
        source = class_visitors[src].get_merge_of_entities()
        destination = class_visitors[dst].get_merge_of_entities()

        similarity = round(tf_idf.apply_tfidf_to_pair(source, destination), 2)
        logging.info(f"{similarity} {src} - {dst}")

        graph[src][dst][str(WeightType.TF_IDF)] = similarity


def prepare_matrix(graph, weight_type=WeightType.ABSOLUTE):
    # https://stackoverflow.com/questions/49064611/how-to-find-different-groups-in-networkx-using-python
    # X = nx.to_numpy_matrix(graph, weight=WEIGHT)

    g = Graph.to_undirected(graph)
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
        Graph.to_undirected(graph))

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

    return [cluster for cluster in clusters.values()]


def visitors_to_qualified_name(visitors):
    qualified_visitors = {}
    for class_name, visitor in visitors.items():
        qualified_name = f"{visitor.get_package_name()}.{class_name}"
        qualified_visitors[qualified_name] = visitor

    return qualified_visitors


def extract_classes_information_from_parsed_json(json_dict):
    classes = {}
    for class_name in json_dict:
        curr = json_dict[class_name]
        annotations = curr['annotations']
        variables = curr['variables']
        dependencies = curr['dependencies']
        temp_methods = curr['methods']
        methods = []

        for method in temp_methods:
            new_method = Method(
                method, temp_methods[method]['parametersDataType'], temp_methods[method]['returnDataType'])
            methods.append(new_method)

        method_invocations = curr['methodInvocations']
        implemented_types = curr['implementedTypes']
        extended_types = curr['extendedTypes']

        new_class = Class(class_name, annotations, variables,
                          dependencies, methods, method_invocations,
                          implemented_types, extended_types)

        classes[class_name] = new_class

    return classes


def create_logging_folders(project_name):
    words_dir = f"{Settings.DIRECTORY}/data/words/{project_name}"
    services_dir = f"{Settings.DIRECTORY}/data/services/{project_name}"
    directories = [words_dir, services_dir]

    for directory in directories:
        if not os.path.exists(directory):
            os.makedirs(directory)


def identify_clusters_in_project(project_name, project_path):
    create_logging_folders(project_name)
    temp_json_location = f'{Settings.DIRECTORY}/data/output.json'

    utils.execute_parser(project_path)

    # Read parsed document
    parsed_raw_json = {}
    with open(temp_json_location) as json_file:
        parsed_raw_json = json.load(json_file)

    classes = extract_classes_information_from_parsed_json(
        parsed_raw_json)

    graph = nx.DiGraph()
    graph = Graph.create_dependencies(classes, graph)

    lda.apply_lda_to_classes(graph, classes)
    calculate_absolute_weights(graph, classes, weight_type=WeightType.LDA)

    # TODO : think about if the pre_processing should be done or not
    graph = Clustering.pre_process(
        graph, remove_weak_edges=False, remove_disconnected_sections=True)

    clusters_results = []
    if Settings.RESOLUTION:
        clusters, modularity = Clustering.community_detection_louvain(
            graph, resolution=Settings.RESOLUTION)
        clusters_results.append((clusters, modularity, Settings.RESOLUTION))
        Clustering.write_modularity_and_services(clusters_results)
    else:
        clusters_results = Clustering.compute_multiple_resolutions(
            graph, start=0.3, end=1.1, step=0.1)

    # TODO: Reconsider techniques of post-processing
    # clusters = PostProcessing.process(clusters, classes, graph.copy())
    return clusters_results


def main():

    parser = argparse.ArgumentParser()
    parser.add_argument("--project", "-p",
                        help="Project path", required=True)
    parser.add_argument("--stop_words", "-s",
                        help="Path to stopwords file")
    parser.add_argument("--k_topics", "-k",
                        help="Number of topics for given project")
    parser.add_argument("--resolution", "-r",
                        help="Resolution number parameter in Louvain community detection. A value in range of 0.3 to 1 is advised. A smaller resolution will identify smaller communities and vice versa. By default the whole range is tested and communities for each community saved.")
    parser.add_argument("--metrics", "-m",
                        help="Execute metrics for a given project name after normal parsing and execution (relative path to set root path) (At the current time it does NOT work independently from the identification process)", action="store_true")
    parser.add_argument("--draw", "-d",
                        help="Enable plotting of graphs", action="store_true")
    parser.add_argument("--lda-plotting", "-l",
                        help="Enable plotting of LDA topics", action="store_true")
    args = parser.parse_args()

    Settings.DRAW = True if args.draw else False
    Settings.LDA_PLOTTING = True if args.lda_plotting else False
    Settings.K_TOPICS = int(args.k_topics) if args.k_topics else None
    Settings.RESOLUTION = float(args.resolution) if args.resolution else None

    Settings.set_stop_words(args.stop_words)

    print(f"Setting Directory as: {Settings.DIRECTORY}")

    # Create required paths for operation with Java parser
    os.makedirs(f"{Settings.DIRECTORY}/data/interfaces", exist_ok=True)
    os.makedirs(f"{Settings.DIRECTORY}/data/operations_per_service", exist_ok=True)

    if args.project:
        project_name = str(args.project.split('/')[-1])
        project_path = str(args.project)
        Settings.PROJECT_PATH = project_path
        Settings.PROJECT_NAME = project_name

        # cluster_results = (clusters, modularity, resolution)
        clusters_results = identify_clusters_in_project(
            project_name, project_path)

        metrics = Metrics()
        for cluster in clusters_results:
            Settings.create_id()

            # TODO: Refactor MetricExecutor into ProcessResultOutput and MetricExecutor, currently sharing many responsabilities
            metric_executor = MetricExecutor()
            metric_executor.add_project(project_name, str(cluster[0]))
            metric_executor.dump_to_json_file()

            if args.metrics:
                # TODO: refactor
                metrics.set_metric_executor(metric_executor)
                metrics.set_cluster_results(clusters_results)
                metrics.calculate()

        if args.metrics:
            metrics.save()


def build_plot(resolution, chm, chd, ifn, smq, cmq, irn, opn):
    # Plot 1
    bar_width = 1/6
    r1 = np.arange(len(resolution))
    r2 = [x + bar_width for x in r1]
    r3 = [x + bar_width for x in r2]
    r4 = [x + bar_width for x in r3]
    r5 = [x + bar_width for x in r4]

    plt.subplot(1, 2, 1)
    plt.bar(r1, chm, width=bar_width, label='chm')
    plt.bar(r2, chd, width=bar_width, label='chd')
    plt.bar(r3, ifn, width=bar_width, label='ifn')
    plt.bar(r4, smq, width=bar_width, label='smq')
    plt.bar(r5, cmq, width=bar_width, label='cmq')
    plt.xlabel('resolution', fontweight='bold')
    plt.xticks([r + bar_width for r in range(len(resolution))],
               resolution)
    plt.legend()

    # Plot 2
    bar_width = 1/3
    r6 = np.arange(len(resolution))
    r7 = [x + bar_width for x in r6]

    plt.subplot(1, 2, 2)
    plt.bar(r6, irn, width=bar_width, label='irn')
    plt.bar(r7, opn, width=bar_width, label='opn')
    plt.xlabel('resolution', fontweight='bold')
    plt.xticks([r + bar_width for r in range(len(resolution))],
               resolution)
    plt.legend()

    return plt

if __name__ == '__main__':
    main()

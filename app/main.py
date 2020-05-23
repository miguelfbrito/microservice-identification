from sklearn.cluster import DBSCAN, FeatureAgglomeration, AffinityPropagation
from sklearn.mixture import GaussianMixture
from datetime import datetime
from itertools import cycle
from random import randint
from random import random
import matplotlib.pyplot as plt
import Utils as utils
import networkx as nx
import javalang
import logging
import numpy as np
import LDA as lda
import collections
import igraph as ig
import community
import argparse
import json
import math
import re
import PostProcessing

from StringUtils import StringUtils
from Graph import Graph
from operator import itemgetter
from TfIdf import TfIdf
from WeightType import WeightType
from Clustering import Clustering
from visitors.ClassVisitor import ClassVisitor
from ProcessResultsOutput import ProcessResultsOutput
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

    print(f"CLASSESii: {classes}")

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
                edge_data[str(WeightType.ABSOLUTE)] = float(0.5)
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

                edge_data[str(WeightType.ABSOLUTE)
                          ] = max(float(edge_data[str(weight_type)]), method_call_weight)
        except KeyError as e:
            # TODO : review why does this only happens on a specific case
            edge_data[str(WeightType.ABSOLUTE)] = 0
            print(f"KEY ERROR {e} {src} {dst}")

        print(
            f"\t-> {src} -> {dst} -> {edge_data[str(WeightType.ABSOLUTE)]}")


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
        print(new_class)

    return classes


def identify_clusters_in_project(project):

    project_name = project[0]
    num_topics = project[1]
    directory = '/home/mbrito/git/thesis-web-applications/monoliths/' + project_name

    temp_json_location = f'{Settings.DIRECTORY}/symbolsolver/target/output.json'

    utils.execute_parser(project_name)

    # 1. Read parsed document
    parsed_raw_json = {}
    with open(temp_json_location) as json_file:
        parsed_raw_json = json.load(json_file)

    classes = extract_classes_information_from_parsed_json(
        parsed_raw_json)

    graph = nx.DiGraph()
    graph = Graph.create_dependencies(classes, graph)

    # Method 1. TF-IDF
    # apply_tfidf_to_connections(graph, qualified_visitors)

    # Method 2. LDA
    # TODO : think about if the pre_processing should be done or not
    lda.apply_lda_to_classes(graph, classes, num_topics)
    calculate_absolute_weights(graph, classes, weight_type=WeightType.LDA)

    graph = Clustering.pre_process(
        graph, remove_weak_edges=False, remove_disconnected_sections=True)

    # Cluster by LDA and structural dependencies
    clusters = Clustering.community_detection_louvain(graph)

    with open('./clusters.txt', 'w') as f:
        for cluster in clusters.values():
            for class_name in cluster:
                f.write(f"{class_name}\n")
            f.write("\n\n")
    print(f"BEFORE CLUTERS {clusters}")
    clusters = PostProcessing.process(clusters, classes, graph.copy())
    print(f"FINAL CLUSTERS {clusters}")
    write_services_to_file(clusters, classes)

    return clusters


def write_services_to_file(clusters, classes):
    # service_id, service
    services = Service.extract_services_from_clusters(clusters)
    class_service = Service.get_map_class_service_id(clusters)

    with open(f"{Settings.DIRECTORY}/data/services/{Settings.PROJECT_NAME}_{Settings.ID}", 'w') as f:
        for service_id, service in services.items():
            f.write(f"\nService {service_id}\n")
            for class_name in service.get_classes():
                f.write(f"{class_name}\n")
            f.write("\n")


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
    parser.add_argument("--draw", "-d",
                        help="Enable plotting of graphs", action="store_true")
    args = parser.parse_args()

    Settings.DRAW = True if args.draw else False

    if args.metrics:
        result = ProcessResultsOutput()
        Settings.PROJECT_NAME = args.metrics
        Settings.K_TOPICS = int(args.k_topics)
        Settings.create_id()
        project = (args.metrics, int(args.k_topics))
        clusters = identify_clusters_in_project(project)
        result.add_project(project[0], str(clusters))
        result.dump_to_json_file()
        result.run_java_metrics()

    if args.metrics_condensed:
        projects = [('spring-blog', 7), ('jpetstore', 5),
                    ('monomusiccorp', 8), ('spring-petclinic', 3)]

        results = ProcessResultsOutput()
        for project in projects:
            Settings.PROJECT_NAME = project[0]
            Settings.K_TOPICS = int(project[1])
            Settings.create_id()
            print(f"\n\nStarting project {project[0]}, {project[1]} topics")
            clusters = identify_clusters_in_project(project)
            clusters = [cluster for cluster in clusters.values()]
            results.add_project(project[0], str(clusters))
        results.dump_to_json_file()
        results.run_java_metrics()

    if args.metrics_full:
        projects = [('spring-blog', 7), ('jpetstore', 5),
                    ('monomusiccorp', 8), ('spring-petclinic', 3), ('jforum', 15), ('agilefant', 25)]
        results = ProcessResultsOutput()
        for project in projects:
            Settings.PROJECT_NAME = project[0]
            Settings.K_TOPICS = int(project[1])
            Settings.create_id()
            print(f"\n\nStarting project {project[0]}, {project[1]} topics")
            print(f"\n\nStarting project {project}")
            clusters = identify_clusters_in_project(project)
            clusters = [cluster for cluster in clusters.values()]
            results.add_project(project[0], str(clusters))
        results.dump_to_json_file()
        results.run_java_metrics()


main()

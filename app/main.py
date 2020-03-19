from FileUtils import FileUtils
from ClassVisitor import ClassVisitor
from operator import itemgetter
from TfIdf import TfIdf

import re
import math
import LDA as lda

import logging
import javalang
import networkx as nx
import matplotlib.pyplot as plt
from random import randint
from random import random


def read_files(files):
    read_files = {}
    for f in files:

        with open(f, 'r') as reader:
            matches = re.search(r"\/(?P<class_name>\w*)\.java", str(f))
            class_name = matches.groupdict()['class_name']
            read_files[class_name] = {}
            read_files[class_name]["fullpath"] = str(f)
            read_files[class_name]["text"] = reader.read()

    return read_files


def create_graph_dependencies(visitors, graph):
    """ Based on the AST counts the number of connections and sets them as weight on a graph """

    for visitor in visitors:
        for dependency in visitor.get_dependencies():
            if visitor.get_class_name() == dependency:
                continue

            curr_edge_weight = graph.get_edge_data(
                visitor.get_class_name(), dependency)
            if curr_edge_weight:
                print(curr_edge_weight)
                graph[visitor.get_class_name(
                )][dependency]['weight'] = curr_edge_weight["weight"] + 1
            else:
                graph.add_edge(visitor.get_class_name(),
                               dependency, weight=1)

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
        tree = javalang.parse.parse(values["text"])
        # print(tree)

        visitor = ClassVisitor()
        for _, node in tree:
            visitor.visit(node)

        class_visitors.append(visitor)
    return class_visitors, graph


def draw_graph(graph):
    pos = nx.spring_layout(graph)

    # Drawing of label explained here - https://stackoverflow.com/questions/31575634/problems-printing-weight-in-a-networkx-graph
    new_labels = dict(map(lambda x: ((x[0], x[1]), str(
        x[2]['weight']) if x[2]['weight'] > 0 else ""), graph.edges(data=True)))

    nx.draw_networkx(graph, pos=pos, node_size=200,
                     font_size=9)
    nx.draw_networkx_edge_labels(
        graph, pos, edge_labels=new_labels, font_size=8)
    nx.draw_networkx_edges(graph, pos, width=1, arrows=False)

    plt.show()


def apply_lda_to_classes(class_visitors):

    for cla in class_visitors:
        print(f"Applying LDA to ${cla.get_class_name()}")

        try:
            lda_result = lda.apply_lda_to_text(cla.get_merge_of_strings())

            # For now we only care about documents evaluated individually, hence the 0.
            cla.set_lda(lda_result[0])
            print(cla.get_lda())

        except ValueError:
            logging.warning(
                "Failed to process a file. It probably contains annotations that the parser is not prepared to handle (eg. @interface)")


def apply_tfidf_to_connections(graph, class_visitors):

    edges = graph.edges()

    print(edges)

    tf_idf = TfIdf()
    for src, dst in edges:
        src_text = ""
        dst_text = ""

        # TODO: Optimize by changing this list to a dict and doing O(1) accesses
        for visitor in class_visitors:
            class_name = visitor.get_class_name()
            if src == class_name:
                src_text = class_name

            if dst == class_name:
                dst_text = class_name

        similarity = round(tf_idf.apply_tfidf_to_pair(src_text, dst_text), 2)
        print(f"${similarity} ${src} - ${dst}")

        graph[src][dst]['weight'] = similarity


def heaviest(graph):
    u, v, w = max(graph.edges(data='weight'), key=itemgetter(2))
    return (u, v)


def main():

    logging.basicConfig(filename='logs.log', filemode="w", level=logging.INFO,
                        format="%(asctime)s:%(levelname)s: %(message)s")
    # Enables printing of logs to stdout as well
    # logging.getLogger().addHandler(logging.StreamHandler())

    # project_name = 'simple-blog'
    project_name = 'monomusiccorp'
    directory = '/home/mbrito/git/thesis-web-applications/monoliths/' + project_name
    files = FileUtils.search_java_files(directory)

    files = read_files(files)

    class_visitors, graph = parse_files_to_ast(files)

    graph = create_graph_dependencies(class_visitors, graph)

    clean_irrelevant_dependencies(class_visitors, graph)

    # apply_lda_to_classes(class_visitors)

    # cluster = nx.clustering(graph, weight='weight')
    # print(cluster)

    apply_tfidf_to_connections(graph, class_visitors)

    draw_graph(graph)

    # Girvan Newman #1
    # communities_generator = nx.algorithms.community.girvan_newman(graph)
    # top_level_communities = next(communities_generator)
    # next_level_communities = next(communities_generator)
    # girvan = sorted(map(sorted, next_level_communities))
    # print(girvan)
    # print(len(girvan))

    # Girvan Newman #2
    # edges = graph.edges()
    # nx.set_edge_attributes(graph, {(u, v): v for u, v in edges}, 'weight')
    # comp = nx.algorithms.community.centrality.girvan_newman(
    #     graph, most_valuable_edge=heaviest)
    # girv = tuple(sorted(c) for c in next(comp))
    # print(girv)
    # print(len(girv))


main()

from FileUtils import FileUtils
from ClassVisitor import ClassVisitor


import re
import LDA as lda
import logging
import javalang
import networkx as nx
import matplotlib.pyplot as plt


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

            weight = 1
            curr_edge_weight = graph.get_edge_data(
                visitor.get_class_name(), dependency)
            if curr_edge_weight:
                weight = curr_edge_weight["weight"] + 1

            graph.add_edge(visitor.get_class_name(),
                           dependency, weight=weight)

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

    # Drawing of weights explained here - https://stackoverflow.com/questions/31575634/problems-printing-weight-in-a-networkx-graph
    new_labels = dict(map(lambda x: ((x[0], x[1]), str(
        x[2]['weight'] if x[2]['weight'] <= 3 else "")), graph.edges(data=True)))

    nx.draw_networkx(graph, pos=pos)
    nx.draw_networkx_edge_labels(graph, pos, edge_labels=new_labels)
    nx.draw_networkx_edges(graph, pos, width=2, arrows=False)

    plt.show()


def apply_lda_to_files(text):

    lda.apply_lda_to_text(text)

    # pairs_edges = graph.edges()
    # print(pairs_edges)
    # for source, target in pairs_edges:
    #     print(source + "  " + target)


def main():

    logging.basicConfig(filename='logs.log', filemode="w", level=logging.INFO,
                        format="%(asctime)s:%(levelname)s: %(message)s")
    # Also prints the logs to stdout
    # logging.getLogger().addHandler(logging.StreamHandler())

    # project_name = 'simple-blog'
    project_name = 'monomusiccorp'
    directory = '/home/mbrito/git/thesis-web-applications/monoliths/' + project_name
    files = FileUtils.search_java_files(directory)

    files = read_files(files)

    # Parse every file with javalang and create an AST
    class_visitors, graph = parse_files_to_ast(files)
    # class_visitors = []

    graph = create_graph_dependencies(class_visitors, graph)

    clean_irrelevant_dependencies(class_visitors, graph)

    for cla in class_visitors:
        print(f"Applying LDA to ${cla.get_class_name()}")

        try:
            apply_lda_to_files(cla.get_merge_of_strings())
        except ValueError:
            logging.warning(
                "Failed to process a file. It probably contains annotations that the parser is not prepared to handle (eg. @interface)")
            pass


main()

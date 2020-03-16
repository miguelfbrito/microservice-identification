from FileUtils import FileUtils
from ClassVisitor import ClassVisitor
import javalang
import networkx as nx
import matplotlib.pyplot as plt


def read_files(files):
    read_files = {}
    for f in files:
        print(f"Reading {str(f)}")

        with open(f, 'r') as reader:
            read_files[str(f)] = reader.read()
    return read_files


def create_graph_dependencies(visitors, graph):
    for visitor in visitors:
        for dependency in visitor.get_dependencies():
            graph.add_edge(visitor.get_class_name(), dependency, weight=2)
    return graph


def clean_irrelevant_dependencies(visitors, graph):
    classes = [visitor.get_class_name() for visitor in visitors]
    graph = create_graph_dependencies(visitors, graph)
    nodes = list(graph.nodes)

    # Iterate over nodes and remove the ones not present in classes
    for node in nodes:
        try:
            if node not in classes:
                graph.remove_node(node)
        except nx.exception.NetworkXError:
            print("Node not found while removing")


def parse_files_to_ast(read_files):
    visitors = []
    graph = nx.DiGraph()
    for _, text in read_files.items():
        tree = javalang.parse.parse(text)

        visitor = ClassVisitor()
        for _, node in tree:
            visitor.visit(node)

        visitors.append(visitor)
    return visitors, graph


def draw_graph(graph):
    pos = nx.spring_layout(graph)

    # Drawing of weights explained here - https://stackoverflow.com/questions/31575634/problems-printing-weight-in-a-networkx-graph
    new_labels = dict(map(lambda x: ((x[0], x[1]), str(
        x[2]['weight'] if x[2]['weight'] <= 3 else "")), graph.edges(data=True)))

    nx.draw_networkx(graph, pos=pos)
    nx.draw_networkx_edge_labels(graph, pos, edge_labels=new_labels)
    nx.draw_networkx_edges(graph, pos, width=2, arrows=False)

    plt.show()


def main():
    # 1. Get all java files from the project
    project_name = 'simple-blog'
    # project_name = 'monomusiccorp'
    directory = '/home/mbrito/git/thesis-web-applications/monoliths/' + project_name
    files = FileUtils.search_java_files(directory)

    # Read the files
    read_text = read_files(files)

    # Parse every file with javalang and create an AST
    visitors, graph = parse_files_to_ast(read_text)

    clean_irrelevant_dependencies(visitors, graph)

    draw_graph(graph)


main()

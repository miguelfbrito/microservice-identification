from FileUtils import FileUtils
from ClassVisitor import ClassVisitor
import javalang
import networkx as nx
import matplotlib.pyplot as plt


def create_graph_dependencies(visitors, graph):
    for visitor in visitors:
        for dependency in visitor.get_dependencies():
            graph.add_edge(visitor.get_class_name(), dependency)
    return graph


def main():
    # 1. Get all java files from the project
    # project_name = 'simple-blog'
    project_name = 'monomusiccorp'
    directory = '/home/mbrito/git/thesis-web-applications/monoliths/' + project_name
    files = FileUtils.search_java_files(directory)

    # 2. Read the files
    read_files = {}
    for f in files:
        print(f"Reading {str(f)}")

        with open(f, 'r') as reader:
            read_files[str(f)] = reader.read()

    # 3. Parse every file with javalang and create an AST
    visitors = []
    graph = nx.DiGraph()
    for _, text in read_files.items():
        tree = javalang.parse.parse(text)

        visitor = ClassVisitor()
        for _, node in tree:
            visitor.visit(node)

        visitors.append(visitor)

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

    # 5. Create a graph
    # graph = nx.DiGraph()
    # graph.add_edge(1, 2)
    # graph.add_edge(2, 3)
    # pos = nx.spring_layout(graph)
    # nx.draw_networkx(graph)
    # plt.show()

    pos = nx.spring_layout(graph, k=0.1)
    nx.draw_networkx(graph)
    plt.show()


main()

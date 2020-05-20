import re
import sys
import logging
import networkx as nx
import matplotlib.pyplot as plt

from WeightType import WeightType
from Settings import Settings


class Graph:

    @staticmethod
    def create_dependencies(classes, graph):
        """ Based on the data parsed from the AST by the javaparser adds and weighted edge.
            Edges are added to references between 2 classes for every:
                - reference of variable, field, etc.
                - method call invocation in a given class
        """
        for classe in classes.values():

            # Add implement relationships
            Graph.add_edges_dependencies(
                classe, classes, classe.get_implemented_types(), graph, 'IMPLEMENTS')

            # Add inheritance relatonships
            Graph.add_edges_dependencies(
                classe, classes, classe.get_extended_types(), graph, 'EXTENDS')

            # Add edges for references created by variables and method parameters and return types
            Graph.add_edges_dependencies(
                classe, classes, classe.get_dependencies(), graph, 'NORMAL')

            # Add edges for method call invocations to other classes
            Graph.add_edges_dependencies(
                classe, classes, [m['targetClassName'] for m in classe.get_method_invocations()], graph, 'METHOD_CALL')

        return graph

    @staticmethod
    def add_edges_dependencies(curr_classe, classes, dependencies, graph, dependency_type):
        for dependency_name in dependencies:
            qualified_name = curr_classe.get_qualified_name()

            try:
                # TODO : Go back to reconsidering the type of dependency
                # ('CLASS_NAME', '{TYPE}')  TYPE = {'EXTENDS', 'IMPLEMENTS', 'NORMAL'}

                # Ignore duplicates (when a class references itself)
                if qualified_name == dependency_name:
                    continue

                edge_data = graph.get_edge_data(
                    qualified_name, dependency_name)

                if edge_data:
                    # Always incremenent the structural component which represents an accumulative sum of all dependencies
                    graph[qualified_name][dependency_name][str(
                        WeightType.STRUCTURAL)] = edge_data.get(str(WeightType.STRUCTURAL), 0) + 1

                    if dependency_type == 'METHOD_CALL':
                        graph[qualified_name][dependency_name][str(
                            WeightType.METHOD_CALL)] = edge_data.get(str(WeightType.METHOD_CALL), 0) + 1

                    # TODO : reconsider this due to new changes
                    # We will not update the type because the first time we set it, it will be set for
                    # EXTENDS or IMPLEMENTs which have higher priority
                else:
                    args = {'weight_structural': 1,
                            'dependency_type': dependency_type}
                    if dependency_type == 'METHOD_CALL':
                        args[str(WeightType.METHOD_CALL)] = 1

                    graph.add_edge(qualified_name,
                                   dependency_name, **args)

            except KeyError:
                # This should only happen when looking for classes that aren't explicly definied in the project
                # instead belong to framewors or external libraries
                logging.error(
                    f"Key not found for {dependency_name} at {qualified_name}")

                # This exception can cause a node to not be added. Verify and add if needed
                if not graph.has_node(qualified_name):
                    graph.add_node(qualified_name)

    @staticmethod
    def clean_irrelevant_dependencies(visitors, graph):
        classes = list(visitors.keys())
        nodes = list(graph.nodes)

        # Iterate over nodes and remove the ones not present in classes
        for node in nodes:
            try:
                if node not in classes:
                    graph.remove_node(node)
            except nx.exception.NetworkXError:
                print("Node not found while removing")

    @staticmethod
    def draw(graph, colors=[], weight_type=WeightType.ABSOLUTE, clear=True):

        if not Settings.DRAW:
            return

        h = graph.copy()
        if clear:
            mappings = {}
            for node in h.nodes():
                mappings[node] = re.search(r'\.(\w*)$', node)[1]
            h = nx.relabel_nodes(h, mappings)

        sp = nx.spring_layout(h, weight=str(weight_type))
        edge_weight_labels = dict(map(lambda x: (
            (x[0], x[1]),  round(x[2][weight_type], 2) if x[2][weight_type] > 0 else ""), h.edges(data=True)))
        nx.draw_networkx_edge_labels(
            h, sp, edge_labels=edge_weight_labels, font_size=8)

        if len(colors) == 0:
            [0 for cluster in h.nodes()]

        for src, dst in h.edges():
            print(f"EDGE: {src} -> {dst} {h.get_edge_data(src, dst)}")

        nx.draw_networkx(h, pos=sp, with_labels=True,
                         node_size=250, node_colors=colors, font_size=8)
        plt.show()

    @staticmethod
    def normalize_values(graph, dependency_type):
        min = sys.maxsize
        max = -sys.maxsize - 1
        for src, dst in graph.edges():
            edge_data = graph.get_edge_data(src, dst)
            if dependency_type in edge_data:
                edge = edge_data[dependency_type]
                if edge < min:
                    min = edge
                if edge > max:
                    max = edge

        for src, dst in graph.edges():
            edge_data = graph.get_edge_data(src, dst)

            if dependency_type in edge_data:
                edge = edge_data[dependency_type]
                edge = (edge - min) / (max - min)

                graph[src][dst][dependency_type] = edge
        return graph

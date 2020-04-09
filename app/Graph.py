from WeightType import WeightType
import networkx as nx
import logging


class Graph:

    @staticmethod
    def create_dependencies(visitors, graph):
        """ Based on the AST counts the number of connections and sets them as weight on a graph """

        for class_name, visitor in visitors.items():
            qualified_name = f"{visitor.get_package_name()}.{class_name}"

            if len(visitor.get_dependencies()) == 0:
                graph.add_node(qualified_name)
                continue

            for dependency in visitor.get_dependencies():

                try:
                    dependency_visitor = visitors[dependency[0]]
                    dependency_name = f"{dependency_visitor.get_package_name()}.{dependency_visitor.get_class_name()}"
                    dependency_type = dependency[1]

                    # Remove duplicates (when a class references itself)
                    if qualified_name == dependency_name:
                        continue

                    edge_data = graph.get_edge_data(
                        qualified_name, dependency_name)

                    if edge_data:
                        graph[qualified_name][dependency_name][str(
                            WeightType.STRUCTURAL)] = edge_data[str(WeightType.STRUCTURAL)] + 1
                        # We will not update the type because the first time we set it, it will be set for
                        # EXTENDS or IMPLEMENTs which have higher priority
                    else:
                        graph.add_edge(qualified_name,
                                       dependency_name, weight_structural=1, dependency_type=dependency_type)

                except KeyError:
                    # This should only happen when looking for classes that aren't explicly definied in the project
                    # instead belong to framewors or external libraries
                    logging.warning(
                        f"Key not found for {dependency} at {qualified_name}")
        return graph

    @staticmethod
    def clean_irrelevant_dependencies(visitors, graph):
        classes = list(visitors.keys())
        print(classes)
        nodes = list(graph.nodes)

        # Iterate over nodes and remove the ones not present in classes
        for node in nodes:
            try:
                if node not in classes:
                    graph.remove_node(node)
            except nx.exception.NetworkXError:
                print("Node not found while removing")

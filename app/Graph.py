from WeightType import WeightType
import networkx as nx


class Graph:

    @staticmethod
    def create_dependencies(visitors, graph):
        """ Based on the AST counts the number of connections and sets them as weight on a graph """

        for class_name, visitor in visitors.items():

            for dependency in visitor.get_dependencies():

                dependency_name = dependency[0]
                dependency_type = dependency[1]

                # In order to remove duplicates (when a class references itself)
                if class_name == dependency_name:
                    continue

                edge_data = graph.get_edge_data(class_name, dependency_name)

                if edge_data:
                    graph[class_name][dependency_name][str(
                        WeightType.STRUCTURAL)] = edge_data[str(WeightType.STRUCTURAL)] + 1
                    # We will not update the type because the first time we set it, it will be set for
                    # EXTENDS or IMPLEMENTs which have higher priority
                else:
                    graph.add_edge(class_name,
                                   dependency_name, weight_structural=1, dependency_type=dependency_type)

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

import re
import logging
import networkx as nx
import matplotlib.pyplot as plt

from WeightType import WeightType


class Graph:

    @staticmethod
    def create_dependencies(classes, graph):
        """ Based on the data parsed from the AST by the javaparser adds and weighted edge.
            Edges are added to references between 2 classes for every:
                - reference of variable, field, etc.
                - method call invocation in a given class
        """
        for classe in classes.values():

            if classe.get_qualified_name() == 'org.springframework.samples.petclinic.vet.Specialty':
                print(
                    f"SPECIALTYDEPENDENCYCLASSE {classe.get_extended_types()}")

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
                classe, classes, classe.get_method_invocations().values(), graph, 'METHOD_CALL')

        return graph

    @staticmethod
    def add_edges_dependencies(curr_classe, classes, dependencies, graph, dependency_type):
        for dependency_name in dependencies:
            qualified_name = curr_classe.get_qualified_name()

            if dependency_name == 'org.springframework.samples.petclinic.vet.Specialty':
                print(
                    f"SPECIALTYDEPENDENCY {dependency_name} {dependency_type}")

            try:
                # TODO : Go back to reconsidering the type of dependency
                # ('CLASS_NAME', '{TYPE}')  TYPE = {'EXTENDS', 'IMPLEMENTS', 'NORMAL'}

                # Ignore duplicates (when a class references itself)
                if qualified_name == dependency_name:
                    continue

                edge_data = graph.get_edge_data(
                    qualified_name, dependency_name)

                # TODO: Think about this
                if edge_data:
                    graph[qualified_name][dependency_name][str(
                        WeightType.STRUCTURAL)] = edge_data[str(WeightType.STRUCTURAL)] + 1
                    # TODO : reconsider this due to new changes
                    # We will not update the type because the first time we set it, it will be set for
                    # EXTENDS or IMPLEMENTs which have higher priority
                else:
                    graph.add_edge(qualified_name,
                                   dependency_name, weight_structural=1, dependency_type=dependency_type)

            except KeyError:
                # This should only happen when looking for classes that aren't explicly definied in the project
                # instead belong to framewors or external libraries
                logging.warning(
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
    def draw(graph, colors=[], weight_type=WeightType.ABSOLUTE):

        # Truncate qualified name to simple name
        h = graph.copy()
        mappings = {}
        for node in h.nodes():
            mappings[node] = re.search(r'\.(\w*)$', node)[1]
        h = nx.relabel_nodes(h, mappings)

        sp = nx.spring_layout(h, weight=str(weight_type))
        print(h.nodes())

        # if(colors):
        #     [cluster+1 for cluster in h.nodes()]

        print(f"Values:  {colors}")

        # node_color=values,
        nx.draw_networkx(h, pos=sp, with_labels=True,
                         node_size=250, node_color=colors, font_size=6, label_color="red")
        plt.show()

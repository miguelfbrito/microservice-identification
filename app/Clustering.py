import re
import itertools
import community
import matplotlib
import pandas as pd
import networkx as nx
import matplotlib.pyplot as plt

from Graph import Graph
from random import random
from operator import itemgetter
from WeightType import WeightType
from networkx import edge_betweenness_centrality as betweenness
from networkx.algorithms.community import label_propagation_communities, kernighan_lin_bisection, greedy_modularity_communities, asyn_fluidc
from networkx.algorithms.community.centrality import girvan_newman


class Clustering:

    @staticmethod
    def community_detection_louvain(g, weight_type=WeightType.ABSOLUTE):
        g = g.to_undirected()
        partition = community.best_partition(
            g, weight=str(WeightType.ABSOLUTE))
        values = [partition.get(node) for node in g.nodes()]

        nodes = list(g.nodes)
        clusters = {}
        for index, val in enumerate(values):
            if val not in clusters:
                clusters[val] = []

            clusters[val].append(nodes[index])

        print(f"Total Clusters: {len(clusters)}")
        print(
            f"Total Clusters len>2: {len([cluster for cluster in clusters if len(clusters[cluster]) > 2])}")

        # TODO : refactor and move this section to Graph.draw()
        # Relabel nodes from qualified name (package+classname) for better graph visibility
        h = g.copy()
        mappings = {}
        for node in h.nodes():
            mappings[node] = re.search(r'\.(\w*)$', node)[1]
        h = nx.relabel_nodes(h, mappings)

        # Drawing of labels explained here - https://stackoverflow.com/questions/31575634/problems-printing-weight-in-a-networkx-graph
        sp = nx.spring_layout(h, weight=str(weight_type))
        nx.draw_networkx(h, pos=sp, with_labels=True,
                         node_size=350, font_size=6, node_color=values)

        edge_weight_labels = dict(map(lambda x: ((x[0], x[1]),  str(
            x[2][weight_type]) if x[2][weight_type] > 0 else ""), h.edges(data=True)))
        nx.draw_networkx_edge_labels(
            h, sp, edge_labels=edge_weight_labels, font_size=6, alpha=1)
        # plt.show()

        cluster_distribution = [len(cluster) for cluster in clusters.values()]
        print(f"Cluster distribution: {cluster_distribution}")
        # print(f"Modularity: {community.modularity(partition, g)}")
        plt.show()

        return clusters

    # Girvan Newman Implementations
    @staticmethod
    def girvan_newman(graph):
        communities_generator = nx.algorithms.community.girvan_newman(graph)
        next(communities_generator)
        next_level_communities = next(communities_generator)
        clusters = sorted(map(sorted, next_level_communities))

        for cluster in clusters:
            pass
            # print(cluster)
        print(
            f"Clusters (len>2): {len([cluster for cluster in clusters if len(cluster) > 2])}")
        print(f"Len: {len(clusters)}")

    @staticmethod
    def girvan_newman_weight(graph):
        clusters = nx.algorithms.community.centrality.girvan_newman(
            graph, most_valuable_edge=Clustering.most_central_edge)

        clusters = tuple(sorted(c) for c in next(clusters))
        for cluster in clusters:
            pass
            # print(cluster)
        print(
            f"Clusters (len>2): {len([cluster for cluster in clusters if len(cluster) > 2])}")
        print(f"Len: {len(clusters)}")

    @staticmethod
    def most_central_edge(graph):
        centrality = betweenness(graph, weight='weight_absolute')
        return max(centrality, key=centrality.get)

    # Can we apply this algorithm multiple times?
    @staticmethod
    def kernighan_lin_bisection(graph):
        clusters = kernighan_lin_bisection(
            graph, weight='weight_absolute')
        print(
            f"Clusters (len>2): {len([cluster for cluster in clusters if len(cluster) > 2])}")
        print(f"Len: {len(clusters)}")

    @staticmethod
    def greedy_modularity_communities(graph):
        clusters = list(greedy_modularity_communities(graph))
        print(
            f"Clusters (len>2): {len([cluster for cluster in clusters if len(cluster) > 2])}")
        print(f"Len: {len(clusters)}")

    # Creates too many clusters?
    @staticmethod
    def label_propagation_communities(graph):
        clusters = label_propagation_communities(graph)
        for cluster in clusters:
            pass
            # print(cluster)
        print(f"Len: {len(list(clusters))}")

    # Only works with connected graphs
    @staticmethod
    def asyn_fluidc(graph, k):
        try:
            clusters = asyn_fluidc(graph, k)
            # print(clusters)
        except nx.exception.NetworkXError:
            print("Async fluidc only works with connect graphs")
        print(f"Len: {len(clusters)}")

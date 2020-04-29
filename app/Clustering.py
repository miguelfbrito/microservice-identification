import itertools
import networkx as nx

from operator import itemgetter
from networkx import edge_betweenness_centrality as betweenness
from networkx.algorithms.community import label_propagation_communities, kernighan_lin_bisection, greedy_modularity_communities, asyn_fluidc


class Clustering:

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

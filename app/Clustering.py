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


import numpy as np

from karateclub.node_embedding.neighbourhood import GraRep, DeepWalk, Walklets, NMFADMM, Diff2Vec, BoostNE, NetMF, LaplacianEigenmaps, HOPE, NodeSketch
from karateclub.community_detection.overlapping import EgoNetSplitter, NNSED, DANMF, MNMF, BigClam, SymmNMF
from karateclub.community_detection.non_overlapping import EdMot, LabelPropagation, SCD, GEMSEC
from karateclub.graph_embedding import Graph2Vec, FGSD, GL2Vec, SF, NetLSD, GeoScattering
from karateclub.node_embedding.attributed import BANE, TENE, TADW, FSCNMF, SINE, MUSAE
from karateclub.node_embedding.structural import GraphWave, Role2Vec
from karateclub.node_embedding.meta import NEU


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

        colors = Clustering.create_colors(clusters)
        print(f"Len: {len(clusters)}")
        Graph.draw(graph, colors=colors)

    # Bad results on clustering
    @staticmethod
    def girvan_newman_weight(graph):
        clusters = nx.algorithms.community.centrality.girvan_newman(
            graph, most_valuable_edge=Clustering.most_central_edge)

        clusters = tuple(sorted(c) for c in next(clusters))
        colors = Clustering.create_colors(clusters)

        print(f"Cluster girvan {clusters}")

        Graph.draw(graph, colors=colors)

    # @staticmethod
    # def most_central_edge(graph, weight_type=WeightType.ABSOLUTE):
    #     centrality = betweenness(graph, weight=str(weight_type))
    #     return max(centrality, key=centrality.get)

    @staticmethod
    def most_central_edge(G):
        centrality = betweenness(G)
        max_cent = max(centrality.values())
        # Scale the centrality values so they are between 0 and 1,
        # and add some random noise.
        centrality = {e: c / max_cent for e, c in centrality.items()}
        # Add some random noise.
        centrality = {e: c + random() for e, c in centrality.items()}
        return max(centrality, key=centrality.get)

    @staticmethod
    def kernighan_lin_bisection(graph):
        clusters = kernighan_lin_bisection(
            graph, weight='weight_absolute')
        print(f"Len: {len(clusters)}")

    @staticmethod
    def greedy_modularity_communities(graph):
        clusters = list(greedy_modularity_communities(graph, weight='weight'))
        colors = Clustering.create_colors(clusters)
        Graph.draw(graph, colors)
        print(f"Clusters: {clusters}")

    # Bad results on clustering
    @staticmethod
    def label_propagation_communities(graph):
        clusters = label_propagation_communities(graph)
        colors = Clustering.create_colors(clusters)
        Graph.draw(graph, colors)

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

    @staticmethod
    def create_colors(clusters):
        colors = []
        for index, cluster in enumerate(clusters):
            for node in cluster:
                colors.append(index)
        return colors

    # Another example of Louvain implementation
    @staticmethod
    def community_louvain(G):
        # Starting with an initial partition of the graph and running the Louvain algorithm for Community Detection
        partition = community.best_partition(G, weight='MsgCount')
        print('Completed Louvain algorithm .. . . ')
        values = [partition.get(node) for node in G.nodes()]
        list_com = partition.values()

        # Creating a dictionary like {community_number:list_of_participants}
        dict_nodes = {}

        # Populating the dictionary with items
        for each_item in partition.items():
            community_num = each_item[1]
            community_node = each_item[0]
            if community_num in dict_nodes:
                value = dict_nodes.get(community_num) + \
                    ' | ' + str(community_node)
                dict_nodes.update({community_num: value})
            else:
                dict_nodes.update({community_num: community_node})

        # Creating a dataframe from the diet, and getting the output into excel
        # community_df = pd.DataFrame.from_dict(
        #     dict_nodes, orient='index', columns=['Members'])
        # community_df.index.rename('Community_Num', inplace=True)
        # community_df.to_csv('Community_List_snippet.csv')

        # Creating a new graph to represent the communities created by the Louvain algorithm
        matplotlib.rcParams['figure.figsize'] = [12, 8]
        G_comm = nx.Graph()

        # Populating the data from the node dictionary created earlier
        G_comm.add_nodes_from(dict_nodes)

        # Calculating modularity and the total number of communities
        mod = community.modularity(partition, G)
        print("Modularity: ", mod)
        print("Total number of Communities=", len(G_comm.nodes()))

        # Creating the Graph and also calculating Modularity
        matplotlib.rcParams['figure.figsize'] = [12, 8]
        pos_louvain = nx.spring_layout(G_comm)
        nx.draw_networkx(G_comm, pos_louvain, with_labels=True, node_size=160, font_size=11, label='Modularity =' + str(round(mod, 3)) +
                         ', Communities=' + str(len(G_comm.nodes())))
        plt.suptitle('Community structure (Louvain Algorithm)',
                     fontsize=22, fontname='Arial')
        plt.box(on=None)
        plt.axis('off')
        plt.legend(bbox_to_anchor=(0, 1), loc='best', ncol=1)
        plt.show()

    # Only works for connected graphs
    @staticmethod
    def gemsec(g):
        model = GEMSEC()

        model.fit(g)
        memberships = model.get_memberships()

        print(f"Memberships: {memberships}")

    @staticmethod
    def geo_scattering(g):

        # model = GEMSEC()

        # model.fit(g)
        # memberships = model.get_memberships()
        # print(f"Memberships {memberships}")

        g = g.to_undirected()
        # TODO: rever que provavelmente não contem todos os componentes

        connected_components = nx.connected_components(g)
        nodes_to_remove = []
        for c in connected_components:
            connected_graph = g.copy()
            print(f"Connected components : {c}")
            for node in connected_graph:
                print("Node : " + str(node))
                if node not in c:
                    nodes_to_remove.append(node)

            for node in nodes_to_remove:
                connected_graph.remove_node(node)
                print(f"Removing node {node}")

            print(f"Length connected graph: {len(connected_graph)}")

            print(f"Connectedgraph: {len(connected_graph)}")
            print(f"Connectedgraph: {connected_graph}")

            numeric_indices = [index for index in range(
                connected_graph.number_of_nodes())]
            node_indices = sorted([node for node in connected_graph.nodes()])

            # Relabel nodes from name to indice to meet KarateClub requirements
            mappings = {}
            for index, node in enumerate(connected_graph.nodes()):
                mappings[node] = index

            print(mappings)
            connected_graph = nx.relabel_nodes(connected_graph, mappings)
            print(numeric_indices)
            print(node_indices)

            model = NodeSketch()
            model.fit(connected_graph)

            print(f"Result: {model.get_cluster_centers()}")

            nx.draw(connected_graph)
            plt.show()

    @staticmethod
    def k_core(graph):
        G_core_30 = nx.k_core(graph, 5)
        # similarly, with at least degree 60
        G_core_60 = nx.k_core(graph, 2)

        print(f"30 {G_core_30}")
        print(f"60 {G_core_60}")

        # Visualize network and k-cores

        plt.rcParams.update(plt.rcParamsDefault)
        plt.rcParams.update({'figure.figsize': (15, 10)})
        pos = nx.spring_layout(graph, k=0.5)
        nx.draw_networkx(
            graph, pos=pos, node_size=0, edge_color="blue", alpha=0.5, with_labels=False)
        nx.draw_networkx(
            G_core_30, pos=pos, node_size=100, edge_color="green", alpha=0.5, with_labels=False)
        nx.draw_networkx(
            G_core_60, pos=pos, node_size=100, edge_color="red", alpha=0.5, with_labels=False)

        plt.show()

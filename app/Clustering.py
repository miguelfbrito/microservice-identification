import re
import itertools
import community
import matplotlib
import numpy as np
import pandas as pd
import networkx as nx
import matplotlib.pyplot as plt

from Graph import Graph
from random import random
from operator import itemgetter
from networkx.drawing.nx_pydot import write_dot
from networkx import edge_betweenness_centrality as betweenness
from networkx.algorithms.community import label_propagation_communities, kernighan_lin_bisection, greedy_modularity_communities, asyn_fluidc, asyn_lpa_communities
from networkx.algorithms.community.centrality import girvan_newman


from karateclub.node_embedding.neighbourhood import GraRep, DeepWalk, Walklets, NMFADMM, Diff2Vec, BoostNE, NetMF, LaplacianEigenmaps, HOPE, NodeSketch
from karateclub.community_detection.overlapping import EgoNetSplitter, NNSED, DANMF, MNMF, BigClam, SymmNMF
from karateclub.community_detection.non_overlapping import EdMot, LabelPropagation, SCD, GEMSEC
from karateclub.graph_embedding import Graph2Vec, FGSD, GL2Vec, SF, NetLSD, GeoScattering
from karateclub.node_embedding.attributed import BANE, TENE, TADW, FSCNMF, SINE, MUSAE
from karateclub.node_embedding.structural import GraphWave, Role2Vec
from karateclub.node_embedding.meta import NEU

from WeightType import WeightType
from entities.Service import Service

from Settings import Settings
from sklearn import cluster


class Clustering:

    @staticmethod
    def community_detection_louvain(graph, weight_type=WeightType.ABSOLUTE):
        weight_type = str(weight_type)
        graph = graph.to_undirected()

        partition = community.best_partition(
            graph, weight=str(WeightType.ABSOLUTE))
        values = [partition.get(node) for node in graph.nodes()]

        nodes = list(graph.nodes)
        clusters = {}
        for index, val in enumerate(values):
            if val not in clusters:
                clusters[val] = []

            clusters[val].append(nodes[index])

        print(f"Total Clusters: {len(clusters)}")

        # TODO : refactor and move this section to Graph.draw()
        # Relabel nodes from qualified name (package+classname) to classname for better graph visibility
        # This can cause problems if there are 2 classes with the same name on different packages
        # eg. com.blog.controllers.PostController and com.blog.admin.PostController
        h = graph.copy()
        mappings = {}

        cluster_distribution = [len(cluster) for cluster in clusters.values()]
        print(f"Cluster distribution: {cluster_distribution}")
        print(f"Modularity: {community.modularity(partition, graph)}")

        if Settings.DRAW:

            for index, node in enumerate(h.nodes()):
                curr_class_name = re.search(r'\.(\w*)$', str(node))
                if curr_class_name:
                    mappings[node] = f"{curr_class_name[1]}_{index}"
            h = nx.relabel_nodes(h, mappings)

            # Drawing of labels explained here - https://stackoverflow.com/questions/31575634/problems-printing-weight-in-a-networkx-graph
            sp = nx.spring_layout(h, weight=weight_type, seed=1)
            nx.draw_networkx(h, pos=sp, with_labels=True,
                             node_size=350, font_size=6, node_color=values)

            edge_weight_labels = dict(map(lambda x: (
                (x[0], x[1]),  round(x[2][str(weight_type)], 2) if x[2][weight_type] > 0 else ""), h.edges(data=True)))

            nx.draw_networkx_edge_labels(
                h, sp, edge_labels=edge_weight_labels, font_size=5, alpha=1)

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
        print(f"CLUSTERS: {clusters}")
        colors = Clustering.create_colors(clusters)

        print(f"Cluster girvan {clusters}")

        Graph.draw(graph, colors=colors)
        return clusters

    # @staticmethod
    # def most_central_edge(graph, weight_type=WeightType.ABSOLUTE):
    #     centrality = betweenness(graph, weight=weight_type)
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
            graph, weight=str(WeightType.ABSOLUTE))
        print(f"Len: {len(clusters)}")

    @staticmethod
    def greedy_modularity_communities(graph):
        clusters = list(greedy_modularity_communities(
            graph, weight=str(WeightType.ABSOLUTE)))
        colors = Clustering.create_colors(clusters)
        Graph.draw(graph, colors)
        print(f"Clusters: {clusters}")

    @staticmethod
    def label_propagation_communities(graph):
        clusters = label_propagation_communities(graph)
        clusters = list(clusters)
        colors = Clustering.create_colors(clusters)
        Graph.draw(graph, colors)

        print(f"Len: {len(list(clusters))}")
        return list(clusters)

    @staticmethod
    def async_label_propagation_communities(graph, weight=WeightType.ABSOLUTE):
        clusters = asyn_lpa_communities(graph)
        clusters = list(clusters)
        colors = Clustering.create_colors(clusters)
        Graph.draw(graph, colors)
        return list(clusters)

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

    @staticmethod
    def cosine_similarity(vector1, vector2):
        return np.dot(vector1, vector2) / (np.linalg.norm(vector1) * np.linalg.norm(vector2))

    @staticmethod
    def pre_process(graph, remove_weak_edges=False, remove_disconnected_sections=False):
        # TODO: could be optimized by caching already traversed nodes
        graph = graph.to_undirected()

        # Remove edges with weak weights. Could have a moderate impact on louvain due to the way it decides which community to choose
        edges_remove = []
        if remove_weak_edges:
            for edge in graph.edges:
                data = graph.get_edge_data(edge[0], edge[1])
                if data and data[str(WeightType.ABSOLUTE)] <= 0.3:
                    edges_remove.append((edge[0], edge[1]))

            for edge in edges_remove:
                graph.remove_edge(edge[0], edge[1])
                print(f"Removing edge (=0) {edge}")

        # Remove nodes that belong to a disconnected section consisting of less than [node_depth] nodes
        nodes_remove = []
        if remove_disconnected_sections:
            for node in graph.nodes():
                node_depth = 5
                edges = nx.dfs_edges(graph, source=node,
                                     depth_limit=node_depth)
                count = 0

                for edge in edges:
                    count += 1

                if count < node_depth:
                    nodes_remove.append(node)

            for node in nodes_remove:
                graph.remove_node(node)
                print(f"Removing node (<{node_depth} dfs) {node}")

        return graph

    @staticmethod
    def create_service_graph_dependency_based(clusters, classes, graph):
        services = Service.extract_services_from_clusters(
            clusters)  # service_id, service
        class_service = Service.get_map_class_service_id(clusters)

        service_graph = nx.DiGraph()
        for service_id in services.keys():
            service_graph.add_node(service_id)

        for src, dst in graph.edges():
            edge_data = graph.get_edge_data(src, dst)

            # TODO: consider add both connections, structural and method call
            src_service_id = class_service[src]
            dst_service_id = class_service[dst]
            if src_service_id != dst_service_id:  # 'method_call_weight' in edge_data and

                service_edge_data = service_graph.get_edge_data(
                    src_service_id, dst_service_id)

                if service_edge_data:
                    service_edge_data[str(WeightType.STRUCTURAL)] += 1
                else:
                    service_graph.add_edge(
                        src_service_id, dst_service_id, weight_structural=1)  # TODO: Rework, use **dict to expand dict as arguments

        Graph.draw(service_graph, clear=False,
                   weight_type=str(WeightType.STRUCTURAL))

        return service_graph, services

    @staticmethod
    def create_service_graph_method_invocation_based(clusters, classes, graph):
        services = Service.extract_services_from_clusters(
            clusters)  # service_id, service
        class_service = Service.get_map_class_service_id(clusters)

        service_graph = nx.DiGraph()
        for service_id in services.keys():
            service_graph.add_node(service_id)

        for src, dst in graph.edges():
            edge_data = graph.get_edge_data(src, dst)
            # TODO: consider add both connections, structural and method call
            try:
                src_service_id = class_service[src]
                dst_service_id = class_service[dst]
                if src_service_id != dst_service_id and str(WeightType.METHOD_CALL) in edge_data:
                    service_edge_data = service_graph.get_edge_data(
                        src_service_id, dst_service_id)

                    if service_edge_data:
                        service_edge_data[str(
                            WeightType.METHOD_CALL)] += edge_data[str(WeightType.METHOD_CALL)]
                    else:
                        service_graph.add_edge(
                            src_service_id, dst_service_id, weight_method_call=1)  # TODO: Rework, use **dict to expand dict as arguments
            except KeyError as ke:
                print(f"[KEYERROR] {ke}")

        # Graph.draw(service_graph, clear=False,
        #            weight_type=str(WeightType.METHOD_CALL))

        return service_graph, services

    @staticmethod
    def merge_above_threshold(service_graph, dependency_type, threshold=0.5):
        services_index = {}
        index = 0
        edges_for_removal = []
        for src, dst in service_graph.edges():
            edge_data = service_graph.get_edge_data(src, dst)
            if dependency_type in edge_data:
                if edge_data[str(dependency_type)] < threshold:
                    edges_for_removal.append((src, dst))

        for edge in edges_for_removal:
            service_graph.remove_edge(edge[0], edge[1])

        index = 0
        visited = {}
        for node in service_graph.nodes():
            dfs_nodes = nx.dfs_preorder_nodes(service_graph, source=node)

            if node in visited:
                continue

            visited[node] = True

            for dfs_node in dfs_nodes:
                if index in services_index:
                    services_index[index].append(dfs_node)
                else:
                    services_index[index] = [dfs_node]

            index += 1

        print(f"Merge above threshold {services_index}")

    @staticmethod
    def k_means(G, k):
        edge_mat = Clustering.graph_to_edge_matrix(G)
        kmeans = cluster.KMeans(n_clusters=k, n_init=200).fit(edge_mat)

        print(f"RESULTS: {kmeans.labels_}")

        services = {}
        # TODO : finish
        for service in kmeans.labels_:
            services.get(service, []).append(service)
        print(f"KMEANS RESULTS: {services}")

    @staticmethod
    def graph_to_edge_matrix(G):
        """Convert a networkx graph into an edge matrix.
        See https://www.wikiwand.com/en/Incidence_matrix for a good explanation on edge matrices

        Parameters
        ----------
        G : networkx graph
        """
        # Initialize edge matrix with zeros
        edge_mat = np.zeros((len(G), len(G)), dtype=int)

        # Loop to set 0 or 1 (diagonal elements are set to 1)
        for node in G:
            for neighbor in G.neighbors(node):
                edge_mat[node][neighbor] = 1
            edge_mat[node][node] = 1

        return edge_mat

    @staticmethod
    def test_clustering_algorithms(graph):
        print("\nGirvan Method 1")
        Clustering.girvan_newman(graph)

        print("\nGirvan Method 2")
        Clustering.girvan_newman_weight(graph)

        print("\nKernighan Lin Bisection")
        Clustering.kernighan_lin_bisection(graph)

        print("\nGreedy_modularity_communities")
        Clustering.greedy_modularity_communities(graph)

        print("\nLabel_propagation_communities")
        Clustering.label_propagation_communities(graph)

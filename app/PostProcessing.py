from Clustering import Clustering
from WeightType import WeightType
from Settings import Settings


def process(clusters, classes, G):

    service_graph, services = Clustering.create_service_graph_method_invocation_based(
        clusters, classes, G)

    # service_graph = Graph.normalize_values(
    #     service_graph, dependency_type=str(WeightType.METHOD_CALL))

    # service_clusters = []
    # try:
    #     service_clusters = Clustering.community_detection_louvain(
    #         service_graph, weight_type=str(WeightType.METHOD_CALL))
    # except ValueError:
    #     print(
    #         f"The graph had no method call invocations between nodes. Clustering will not be used")

    merge_services_with_single_relationship_and_no_operations(
        service_graph, services)

    return {service_id: list(service.get_classes()) for service_id, service in services.items()}


def merge_services_with_single_relationship_and_no_operations(service_graph, services):
    # Read classe interfaces identified
    interfaces_path = f"/home/mbrito/git/thesis/data/interfaces/{Settings.PROJECT_NAME}"
    interfaces = set()
    with open(interfaces_path, 'r') as f:
        while True:
            line = f.readline()
            if not line:
                break
            interfaces.add(line.split('\n')[0])

    # For each service check if contains an interface
    for service_id in service_graph.copy().nodes():
        service = services.get(service_id)
        if service:
            service_classes = service.get_classes()
            if len(interfaces & service_classes) == 0:
                edges = service_graph.edges([service_id])
                if len(edges) == 1:
                    print(f"Merging {service_id} to {edges}")
                    merge_services(
                        service_id, list(edges)[0][1], service_graph, services)


def merge_services(src_id, dst_id, service_graph, services):
    # Merge classes
    src_classes = services[src_id].get_classes()
    services[dst_id].set_classes(
        services[dst_id].get_classes().union(src_classes))

    # Remove service node
    service_graph.remove_node(src_id)


def return_as_cluster_string(services):
    return [list(service.get_classes()) for service in services.values()]

import itertools


class Service:

    new_id = itertools.count()

    def __init__(self, service_id=None, classes={}):
        if service_id == None:
            self.id = next(Service.new_id)
        else:
            self.id = service_id
        self.classes = set(classes)
        self.external_classes_dependencies = {}
        self.service_dependencies = {}

    def get_classes(self):
        return self.classes

    def set_classes(self, classes):
        self.classes = classes

    def set_external_classes_dependencies(self, dependencies):
        self.external_classes_dependencies = dependencies

    def get_external_classes_dependencies(self):
        return self.external_classes_dependencies

    def get_service_dependencies(self):
        return self.service_dependencies

    def set_service_dependencies(self, dependencies):
        self.service_dependencies = dependencies

    @staticmethod
    def extract_services_from_clusters(clusters):
        services = {}

        for cluster_id in clusters:
            service = Service(cluster_id, clusters[cluster_id])
            services[cluster_id] = service

        return services

    @staticmethod
    def get_map_class_service_id(clusters):
        class_service = {}
        # {0 : [a, b, c]}
        for service_id in clusters:
            classes = clusters[service_id]
            for classe in classes:
                class_service[classe] = service_id

        return class_service

    def __repr__(self):
        return str(self.classes)

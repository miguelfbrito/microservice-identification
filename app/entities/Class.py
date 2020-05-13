from multipledispatch import dispatch
import math
import logging
import javalang

from StringUtils import StringUtils


class Class:

    # def __init__(self):
    #     # TODO: refactor some of the lists to sets
    #     self.class_name = "FailedToLoadClassName"
    #     self.package_name = "FailedToLoadPackageName"
    #     self.qualified_name = "FailedToLoadQualifiedName"
    #     self.annotations = []
    #     self.dependencies = []
    #     self.variables = []
    #     self.methods = []
    #     self.formal_parameters = []
    #     self.literals = []
    #     self.comments = []

    def __init__(self, qualified_name="FailedToLoadClassName", annotations=[], variables=[], dependencies=[], methods=[], method_invocations={}, implemented_types=[], extended_types=[]):
        self.qualified_name = qualified_name
        self.annotations = annotations
        self.variables = variables
        self.dependencies = dependencies
        self.methods = methods
        self.method_invocations = method_invocations
        self.implemented_types = implemented_types
        self.extended_types = extended_types

    def extract_comments(self, string):
        self.comments = StringUtils.extract_comments_from_string(string)

    def total_words(self, array):
        return sum(len(sentence.split()) for sentence in array)

    def get_merge_of_strings(self):
        logging.info(f"Dependencies: {self.dependencies}")
        dependencies = [dep[0] for dep in self.dependencies]
        # len_words = self.total_words(dependencies) + self.total_words(self.variables) + self.total_words(
        #     self.methods) + self.total_words(self.formal_parameters) + self.total_words(self.literals) + self.total_words(self.comments) + 1

        # TODO : estava a considerar o uso das dependencias antes? Voltar a utilizar?!
        len_words = self.total_words(self.variables) + 1

        # TODO: we could apply other heuristics to try to identify we're currently in an entity
        # TODO: search for the qualified name for Entitities. Entity, DTO, DAO, domain, (common name conventions representing strong domain concepts)
        entity_types = ["Entity", "MappedSuperclass", "Repository"]

        is_entity = False
        for entity in entity_types:
            if entity in self.annotations:
                is_entity = True

        qualified_name_weight = math.ceil(
            len_words * 1) if is_entity else math.ceil(len_words * 0.25)
        dependencies_weight = 1
        variables_weight = 1
        methods_weight = 1
        formal_parameters_weight = 1
        literals_weight = 1
        comments_weight = 1

        # string = dependencies_weight * dependencies + variables_weight * self.variables + methods_weight * \
        #     self.methods

        string = variables_weight * self.variables

        return " ".join(string) + qualified_name_weight * (" " + self.qualified_name)

    def get_class_name(self):
        return self.class_name

    def get_qualified_name(self):
        return self.qualified_name

    def get_dependencies(self):
        return self.dependencies

    def get_methods(self):
        return self.methods

    def get_variables(self):
        return self.variables

    def get_literals(self):
        return self.literals

    def get_package_name(self):
        return self.package_name

    def get_method_invocations(self):
        return self.method_invocations

    def get_implemented_types(self):
        return self.implemented_types

    def get_extended_types(self):
        return self.extended_types

    def __str__(self):
        return f"({self.qualified_name}, {self.methods})"

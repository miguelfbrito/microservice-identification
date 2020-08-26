import re
import math
import logging
import javalang

from StringUtils import StringUtils
from multipledispatch import dispatch


class Class:

    def __init__(self, qualified_name="package.FailedToLoadClassName", annotations=[], variables=[], dependencies=[], methods=[], method_invocations={}, implemented_types=[], extended_types=[]):
        self.qualified_name = qualified_name
        search = re.search(r'\.(\w*)$', qualified_name)
        if search:
            self.class_name = search[1]
        else:
            self.class_name = qualified_name
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
        # Could be more precise using regex, not a big deal
        return sum(len(sentence.split()) for sentence in array)

    def get_merge_of_entities(self):
        logging.info(f"Dependencies: {self.dependencies}")
        dependencies = [dep[0] for dep in self.dependencies]

        # TODO : estava a considerar o uso das dependencias antes? Voltar a utilizar?!

        # len_words = self.total_words(dependencies) + self.total_words(self.variables) + self.total_words(
        #     self.methods) + self.total_words(self.formal_parameters) + self.total_words(self.literals) + self.total_words(self.comments) + 1

        pattern_match_class_name = r'\.(\w*)$'

        len_words = 0
        methods = []
        for m in self.methods:
            method_string = m.get_merge_of_entities()
            len_words += self.total_words(method_string)
            methods.append(method_string)

        method_invocations = [re.search(pattern_match_class_name, m['targetClassName'])
                              [1] for m in self.method_invocations]
        method_invocations_variables = [m['scopeName']
                                        for m in self.method_invocations]
        method_invocations_names = [m['methodName']
                                    for m in self.method_invocations]

        len_words += self.total_words(self.variables) + \
            self.total_words(method_invocations) + \
            self.total_words(method_invocations_variables) + \
            self.total_words(method_invocations_names)

        # TODO: we could apply other heuristics to try to identify we're currently in an entity
        # TODO: search for the qualified name for Entitities. Entity, DAO, domain, (common name conventions representing strong domain concepts)
        entity_types = {"Entity", "MappedSuperclass", "Repository"}

        is_entity = False
        for annotation in self.annotations:
            if annotation in entity_types:
                is_entity = True

        class_name_weight = math.ceil(
            len_words * 0.5) if is_entity else math.ceil(len_words * 0.1)

        if class_name_weight == 0:
            class_name_weight = 1

        variables_weight = 2
        methods_weight = 2
        method_invocations_weight = 1

        string = variables_weight * self.variables + \
            methods_weight * methods + \
            method_invocations_weight * method_invocations + \
            method_invocations_weight * method_invocations_variables + \
            method_invocations_weight * method_invocations_names

        string = " ".join(string) + class_name_weight * (" " + self.class_name)
        return string

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

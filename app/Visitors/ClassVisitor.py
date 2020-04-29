from multipledispatch import dispatch
import math
import logging
import javalang

from StringUtils import StringUtils


class ClassVisitor:

    def __init__(self):
        # TODO: refactor some of the lists to sets
        self.class_name = "FailedToLoadClassName"
        self.package_name = "FailedToLoadPackageName"
        self.annotations = []
        self.dependencies = []
        self.variables = []
        self.methods = []
        self.formal_parameters = []
        self.literals = []
        self.comments = []
        self.lda = None

    def extract_comments(self, string):
        self.comments = StringUtils.extract_comments_from_string(string)

    @dispatch(javalang.tree.Annotation)
    def visit(self, type):
        ignored_annotations = ["Override"]
        if type.name not in ignored_annotations:
            self.annotations.append(str(type.name))
            logging.info("Visiting class annotation: " + str(type.name))

    @dispatch(javalang.tree.InterfaceDeclaration)
    def visit(self, type):
        self.class_name = type.name
        logging.info("Visiting interface declaration: " + str(type.name))

    @dispatch(javalang.tree.PackageDeclaration)
    def visit(self, type):
        self.package_name = type.name
        logging.info("Visiting package declaration: " + str(type))

    @dispatch(javalang.tree.ReferenceType)
    def visit(self, type):
        ignored_reference_types = {"String", "Integer"}

        if type.name not in ignored_reference_types:
            self.dependencies.append((str(type.name), 'NORMAL'))
            logging.info("Visiting reference type: " + str(type.name))
            logging.info(str(type))

    @dispatch(javalang.tree.MemberReference)
    def visit(self, type):
        if type.qualifier != None or len(str(type.qualifier)) > 0:
            self.dependencies.append((str(type.qualifier), 'STATIC'))
            logging.info("Visiting member reference: " + str(type))

    @dispatch(javalang.tree.ClassDeclaration)
    def visit(self, type):
        self.class_name = type.name
        extends = type.extends
        implements = type.implements
        logging.info("Visiting class declaration: " + str(type.name))
        logging.info("-> extends: " + str(extends))
        logging.info("-> implements: " + str(implements))

        if extends:
            self.dependencies.append((str(extends.name), 'EXTENDS'))

        if implements:
            for imp in implements:
                self.dependencies.append((str(imp.name), 'IMPLEMENTS'))

    @dispatch(javalang.tree.FormalParameter)
    def visit(self, type):
        self.formal_parameters.append(str(type.name))
        logging.info("Visiting formal parameter: " + str(type.name))
        pass

    @dispatch(javalang.tree.Literal)
    def visit(self, type):
        self.literals.append(str(type.value))
        logging.info("Visiting literal: " + str(type.value))
        pass

    @dispatch(javalang.tree.MethodDeclaration)
    def visit(self, type):
        self.methods.append(str(type.name))
        logging.info("Visiting method declaration: " + str(type.name))

    # Difference to VariableDeclarator?
    @dispatch(javalang.tree.FieldDeclaration)
    def visit(self, type):
        pass

    @dispatch(javalang.tree.VariableDeclarator)
    def visit(self, type):
        self.variables.append(str(type.name))
        logging.info("Visiting variable declaration: " + str(type.name))

    @dispatch(javalang.tree.Import)
    def visit(self, type):
        # TODO : import static class dependencies from files
        logging.info("Visiting import: " + str(type))

    def total_words(self, array):
        return sum(len(sentence.split()) for sentence in array)

    def get_merge_of_strings(self):

        logging.info(f"Dependencies: {self.dependencies}")
        dependencies = [dep[0] for dep in self.dependencies]
        len_words = self.total_words(dependencies) + self.total_words(self.variables) + self.total_words(
            self.methods) + self.total_words(self.formal_parameters) + self.total_words(self.literals) + self.total_words(self.comments) + 1

        entity_types = ["Entity", "MappedSuperclass", "Repository"]

        is_entity = False
        for entity in entity_types:
            if entity in self.annotations:
                is_entity = True

        class_name_weight = math.ceil(
            len_words * 1) if is_entity else 1
        dependencies_weight = 1
        variables_weight = 1
        methods_weight = 1
        formal_parameters_weight = 1
        literals_weight = 1
        comments_weight = 1

        string = dependencies_weight * dependencies + variables_weight * self.variables + methods_weight * \
            self.methods + formal_parameters_weight * self.formal_parameters + \
            literals_weight * self.literals + comments_weight * self.comments

        return " ".join(string) + class_name_weight * (" " + self.class_name)

    # Wildcard match
    @dispatch(object)
    def visit(self, type):
        pass

    def get_class_name(self):
        return self.class_name

    def get_dependencies(self):
        return self.dependencies

    def get_methods(self):
        return self.methods

    def get_variables(self):
        return self.variables

    def get_literals(self):
        return self.literals

    def set_lda(self, lda):
        self.lda = lda

    def get_lda(self):
        return self.lda

    def get_package_name(self):
        return self.package_name

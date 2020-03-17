from multipledispatch import dispatch
import logging
import javalang


class ClassVisitor:

    def __init__(self):
        self.class_name = ""
        self.dependencies = []
        self.variables = []
        self.methods = []
        self.formal_parameters = []
        self.literals = []

    @dispatch(javalang.tree.InterfaceDeclaration)
    def visit(self, type):
        self.class_name = type.name
        logging.info("Visiting interface declaration: " + str(type.name))

    @dispatch(javalang.tree.ReferenceType)
    def visit(self, type):
        IGNORED_REFERENCE_TYPES = ["String", "Integer"]
        if type.name not in IGNORED_REFERENCE_TYPES:
            self.dependencies.append(str(type.name))
            logging.info("Visiting reference type: " + str(type.name))

    @dispatch(javalang.tree.ClassDeclaration)
    def visit(self, type):
        self.class_name = type.name
        logging.info("Visiting class declaration: " + str(type.name))

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
        # self.variables.append(type)
        # print("Visiting field declaration: " + str(type.name))
        pass

    @dispatch(javalang.tree.VariableDeclarator)
    def visit(self, type):
        self.variables.append(str(type.name))
        logging.info("Visiting variable declaration: " + str(type.name))

    @dispatch(object)
    def visit(self, type):
        pass

    def get_merge_of_strings(self):
        weight_class = 50
        string = self.dependencies + self.variables + \
            self.methods + self.formal_parameters + self.literals
        string = " ".join(string) + weight_class * (" " + self.class_name)
        return string

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

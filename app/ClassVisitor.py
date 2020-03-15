from multipledispatch import dispatch
import javalang


class ClassVisitor:

    def __init__(self):
        self.class_name = ""
        self.dependencies = []

    @dispatch(javalang.tree.ReferenceType)
    def visit(self, type: javalang.tree.ReferenceType):
        IGNORED_REFERENCE_TYPES = ["String", "Integer"]
        if type.name not in IGNORED_REFERENCE_TYPES:
            self.dependencies.append(str(type.name))
        # print("This is a reference type " + str(type))

    @dispatch(javalang.tree.ClassDeclaration)
    def visit(self, type: javalang.tree.ClassDeclaration):
        # print("Class declaration found" + str(type.name))
        self.class_name = type.name

    @dispatch(javalang.tree.ClassCreator)
    def visit(self, type: javalang.tree.ClassCreator):
        pass

    @dispatch(object)
    def visit(self, type):
        pass

    def get_class_name(self):
        return self.class_name

    def get_dependencies(self):
        return self.dependencies

from pathlib import Path
import re


class FileUtils:

    @staticmethod
    def search_java_files(directory):
        return list(Path(directory).rglob('*.java'))

    @staticmethod
    def clear_java_words(string):

        stopwords = {
            "abstract", "assert", "boolean",
            "break", "byte", "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else", "extends", "false",
            "final", "finally", "float", "for", "goto", "if", "implements",
            "import", "instanceof", "int", "interface", "long", "native",
            "new", "null", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super", "switch",
            "synchronized", "this", "throw", "throws", "transient", "true",
            "try", "void", "volatile", "while", "repository", "annotation", "string", "int",
            "gaussic", "controller", "map", "request", "entity", "method", "integer", "system", "out", "println", "springframework", "beans",
            "com", "request", "mapping", "value", "autowired"
        }
        # stopwords = {'public', 'int', 'string',
        #              'private', 'void', 'boolean', 'return', 'import', 'package'}

        resultwords = []
        uncamel_words = re.sub(r'(?<!^)(?=[A-Z])', ' ', string).lower()
        words = re.split(r"\W+", uncamel_words)
        for word in words:
            if word.lower() not in stopwords:
                # Temporary
                if word.lower() not in stopwords:
                    resultwords.append(word)

        return (' ').join(resultwords)

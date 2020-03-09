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
            "try", "void", "volatile", "while"
        }
        # stopwords = {'public', 'int', 'string',
        #              'private', 'void', 'boolean', 'return', 'import', 'package'}

        resultwords = []
        for word in re.split("\W+", string):
            if word.lower() not in stopwords:
                resultwords.append(word)

        return (' ').join(resultwords)
    # TODO next:
    #    - ler os ficheiros
    #    - para cada par aplicar o tfidf

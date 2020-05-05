
import re
import time
import pathlib
from pathlib import Path


class StringUtils:

    @staticmethod
    def search_java_files(directory):
        files = list(Path(directory).rglob('*.java'))
        return [file for file in files if len(re.findall(r'[Tt]ests?', str(file))) == 0]

    @staticmethod
    def remove_comment(comment):
        ignore_words = ["license", "copyright", "author",  "apache"]
        for word in re.findall(r"\w+", comment):
            if word.lower() in ignore_words:
                return True
        return False

    @staticmethod
    def extract_comments_from_string(string):
        slash_comment_pattern = r"\/\/\s?(.*)"
        asterisk_comment_pattern = r"(?s)/\*.*?\*/"

        asterisk_comments = re.findall(asterisk_comment_pattern, string)
        comments = []

        # Ignore comments related with Licenses, copyright, authors, etc.
        for comment in asterisk_comments:
            comment = re.sub(r"[/*\n\s]", " ", comment)
            comment = re.sub(r"\s{1,}", " ", comment).strip()

            if not StringUtils.remove_comment(comment):
                comments.append(comment)

        slash_comments = re.findall(slash_comment_pattern, string)
        for comment in slash_comments:
            if not StringUtils.remove_comment(comment):
                comments.append(comment)

        return comments

    @staticmethod
    def clear_java_words(string):

        class_name = string[0]
        string = string[1]

        if isinstance(string, list):
            string = string[0]

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
            "com", "request", "mapping", "value", "autowired", "list", "hash", "set", "test", "id", "date", "spring", "mvc", "test", "mock", "except", "maven", "impl", "decimal", "serializable", "none", "set", "get", "object", "array"
        }

        result_words = []
        uncamel_words = re.sub(r'(?<!^)(?=[A-Z])', ' ', string).lower()
        words = re.split(r"\W+", uncamel_words)
        for word in words:
            if word.isalpha() and word.lower() not in stopwords and len(word) > 2:
                result_words.append(word)

        # directory = f"../data/classes/"
        # pathlib.Path(directory).mkdir(parents=True, exist_ok=True)
        # with open(f"{directory}/{class_name}", 'w') as f:
        #     f.write((' ').join(result_words))

        return (' ').join(result_words)

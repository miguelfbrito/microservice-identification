from pathlib import Path
import re


class FileUtils:

    @staticmethod
    def search_java_files(directory):
        return list(Path(directory).rglob('*.java'))

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

            if not FileUtils.remove_comment(comment):
                comments.append(comment)

        slash_comments = re.findall(slash_comment_pattern, string)
        for comment in slash_comments:
            if not FileUtils.remove_comment(comment):
                comments.append(comment)

        return comments

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
            "com", "request", "mapping", "value", "autowired", "list", "hash", "set", "test", "id", "date", "spring", "mvc", "test", "mock", "except"
        }

        resultwords = []
        uncamel_words = re.sub(r'(?<!^)(?=[A-Z])', ' ', string).lower()
        words = re.split(r"\W+", uncamel_words)
        for word in words:
            if word.lower() not in stopwords and word.isalpha():
                resultwords.append(word)

        return (' ').join(resultwords)

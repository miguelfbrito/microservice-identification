import re
import time
import pathlib

from pathlib import Path
from Settings import Settings


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
    def clear_text(string):
        stopwords = set()

        if isinstance(string, list):
            string = string[0]

        if Settings.STOP_WORDS_PATH:
            with open(Settings.STOP_WORDS_PATH, 'r') as f:
                lines = f.readlines()
                for line in lines:
                    words = line.split(' ')[:-1]
                    words = [w for w in words if w != '']
                    stopwords.update(words)
        else:
            raise Exception("Stop words path not set")

        result_words = []
        uncamel_words = re.sub(r'(?<!^)(?=[A-Z])', ' ', string).lower()
        words = re.split(r"\W+", uncamel_words)
        # with open('file', 'a+') as f:
        for word in words:
            if word.isalpha() and word.lower() not in stopwords and len(word) > 2:
                result_words.append(word)

        return (' ').join(result_words)

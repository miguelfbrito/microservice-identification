from datetime import datetime
import os


class Settings:
    PROJECT_NAME = ''
    K_TOPICS = None
    RESOLUTION = None
    DRAW = False
    DIRECTORY = "/"+"/".join(os.path.abspath(os.getcwd()
                                             ).split("/")[1:-1])
    DIRECTORY_APPLICATIONS = "/home/mbrito/git/thesis-web-applications/monoliths"

    DIRECTORY_PROJECTS = f"{DIRECTORY}/projects.json"
    LDA_PLOTTING = False
    METRIC_EVALUATION = False

    PROJECT_PATH = ''
    STOP_WORDS_PATH = ''
    MALLET_PATH = "~/.mallet/bin/mallet"

    @staticmethod
    def create_id():
        Settings.ID = datetime.now().strftime(
            f'%d_%m_%H_%M_%S')

    @staticmethod
    def set_stop_words(stop_words_path):
        if stop_words_path:
            Settings.STOP_WORDS_PATH = stop_words_path
        else:
            Settings.STOP_WORDS_PATH = f"{Settings.DIRECTORY}/stop_words.txt"

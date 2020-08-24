from datetime import datetime
import os


class Settings:
    PROJECT_NAME = ''
    PROJET_PATH = ''
    K_TOPICS = 0
    DRAW = False
    DIRECTORY = "/"+"/".join(os.path.abspath(os.getcwd()
                                             ).split("/")[1:-1])
    DIRECTORY_APPLICATIONS = "/home/mbrito/git/thesis-web-applications/monoliths"
    DIRECTORY_PROJECTS = f"{DIRECTORY}/projects.json"
    LDA_PLOTTING = False
    METRIC_EVALUATION = False
    MALLET_PATH = "~/.mallet/bin/mallet"

    @staticmethod
    def create_id():
        Settings.ID = datetime.now().strftime(
            f'%d_%m_%H_%M_%S')

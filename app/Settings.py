from datetime import datetime


class Settings:
    PROJECT_NAME = ''
    PROJET_PATH = ''
    K_TOPICS = 0
    DRAW = False
    DIRECTORY = "/home/mbrito/git/thesis"
    DIRECTORY_APPLICATIONS = "/home/mbrito/git/thesis-web-applications/monoliths"
    DIRECTORY_PROJECTS = "/home/mbrito/git/thesis/projects.json"
    LDA_PLOTTING = False
    METRIC_EVALUATION = False
    # MALLET_PATH = "/home/mbrito/git/Mallet/bin/mallet"
    MALLET_PATH = "~/.mallet/bin/mallet"

    @staticmethod
    def create_id():
        Settings.ID = datetime.now().strftime(
            f'%d_%m_%H_%M_%S')

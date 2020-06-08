from datetime import datetime


class Settings:
    PROJECT_NAME = ''
    K_TOPICS = 0
    DRAW = False
    DIRECTORY = "/home/mbrito/git/thesis"
    DIRECTORY_APPLICATIONS = "/home/mbrito/thesis-web-applications/monoliths"
    DIRECTORY_PROJECTS = "/home/mbrito/git/thesis/projects.json"
    LDA_PLOTTING = False
    METRIC_EVALUATION = False

    @staticmethod
    def create_id():
        Settings.ID = datetime.now().strftime(
            f'%d_%m_%H_%M_%S_K{Settings.K_TOPICS}')

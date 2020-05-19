from datetime import datetime


class Settings:
    PROJECT_NAME = ''
    K_TOPICS = 0
    DRAW = True
    DIRECTORY = "/home/mbrito/git/thesis/"
    ID = ''

    @staticmethod
    def create_id():
        Settings.ID = datetime.now().strftime(
            f'%H_%M_%S-%d_%m__K{Settings.K_TOPICS}')

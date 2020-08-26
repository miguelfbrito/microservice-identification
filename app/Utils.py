import subprocess
from Settings import Settings


def execute_parser(project_path):
    symbol_solver_path = f"{Settings.DIRECTORY}/symbolsolver/target/"
    command = f"java -Dparse -Dproject={project_path} "

    if Settings.STOP_WORDS_PATH:
        command += f"-Dstop_words={Settings.STOP_WORDS_PATH} "

    command += "-cp symbolsolver-1.0.jar Main"

    print(f"Invoking parsing: {command}")
    subprocess.call(command, cwd=symbol_solver_path, shell=True)


def normalize(array):
    min_val = min(array)
    max_val = max(array)

    return [(a - min_val)/(max_val - min_val) for a in array]

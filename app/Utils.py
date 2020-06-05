import subprocess
from Settings import Settings


def execute_parser(project_name):

    symbol_solver_path = f"{Settings.DIRECTORY}/symbolsolver/target/"

    # command = f"mvn package -DskipTests && java -Dproject=\"{project_name}\" -cp target/symbolsolver-1.0.jar Main"
    command = f"java -Dproject={project_name} -cp symbolsolver-1.0.jar Main"

    print(f"COMMAND {command}")

    subprocess.call(command, cwd=symbol_solver_path, shell=True)

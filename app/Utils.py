import subprocess


def execute_parser(project_name):

    symbol_solver_path = "/home/mbrito/git/thesis/symbolsolver/target/"

    # command = f"mvn package -DskipTests && java -Dproject=\"{project_name}\" -cp target/symbolsolver-1.0.jar Main"
    command = f"java -Dproject={project_name} -cp symbolsolver-1.0.jar Main"

    print(f"COMMAND {command}")

    subprocess.call(command, cwd=symbol_solver_path, shell=True)

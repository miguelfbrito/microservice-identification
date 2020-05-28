import json
import subprocess
from Settings import Settings
import metrics.CHD as CHD
import metrics.CHM as CHM


class ProcessResultsOutput:

    def __init__(self):
        super().__init__()
        self.projects = []
        self.commit_hash = str(subprocess.check_output(
            ["git", "rev-parse", "--short", "HEAD"]))

    ROOT_PATH = "/home/mbrito/git/thesis-web-applications/monoliths/"
    PROJECTS_PATH = "/home/mbrito/git/thesis/projects.json"

    def write_to_file(self, path, content, type="w"):
        with open(path, type) as f:
            f.write(content)

    def add_project(self, project_name, cluster_string):
        project = {}
        project["id"] = Settings.ID
        project["name"] = project_name
        project["rootPath"] = ProcessResultsOutput.ROOT_PATH
        project["relativePath"] = project_name
        project["clusterString"] = cluster_string
        project["commitHash"] = self.commit_hash
        self.projects.append(project)

    def dump_to_json_file(self):
        with open(str(ProcessResultsOutput.PROJECTS_PATH), "w") as f:
            f.write(json.dumps(self.projects, indent=4))

    def run_metrics(self):
        # Runs java method responsible for calculating OPN and IRN and extracting method invocations in order to calculate
        # CHM and CHD. Resorts to the projects.json written above as input.
        subprocess.call("mvn -Dtest=GenerateMetricsTest test",
                        cwd="/home/mbrito/git/thesis/symbolsolver/", shell=True)

        # output_fosci.csv is obtained from the execution of the previous metrics on the symbolsolver
        file_path = "/home/mbrito/git/thesis/app/metrics/output_fosci.csv"
        CHM.calculate(file_path)
        CHD.calculate(file_path)

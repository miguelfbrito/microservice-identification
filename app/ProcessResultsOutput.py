import json
import subprocess


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
        project["name"] = project_name
        project["rootPath"] = ProcessResultsOutput.ROOT_PATH
        project["relativePath"] = project_name
        project["clusterString"] = cluster_string
        project["commitHash"] = self.commit_hash
        self.projects.append(project)

    def dump_to_json_file(self):
        print("Writing to json")
        print(self.projects)
        with open(str(ProcessResultsOutput.PROJECTS_PATH), "w") as f:
            f.write(json.dumps(self.projects, indent=4))

    def run_java_metrics(self):
        # Runs java method responsible for calculating the metrics for the projects present in projects.json written above
        subprocess.call("mvn -Dtest=GenerateMetricsTest test",
                        cwd="/home/mbrito/git/thesis/symbolsolver/", shell=True)

        # Opens the file where the metrics are saved
        # subprocess.call(
        #     "vim /home/mbrito/git/thesis/data/results.csv", shell=True)

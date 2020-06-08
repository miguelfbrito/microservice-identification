import json
import subprocess
from Settings import Settings
import metrics.CHD as CHD
import metrics.CHM as CHM
import metrics.IFN as IFN


class ProcessResultsOutput:

    def __init__(self):
        super().__init__()
        self.projects = []
        self.commit_hash = str(subprocess.check_output(
            ["git", "rev-parse", "--short", "HEAD"]))

    def write_to_file(self, path, content, type="w"):
        with open(path, type) as f:
            f.write(content)

    def add_project(self, project_name, cluster_string):
        project = {}
        project["id"] = Settings.ID
        project["name"] = project_name
        project["rootPath"] = Settings.DIRECTORY_APPLICATIONS
        project["relativePath"] = project_name
        project["clusterString"] = cluster_string
        project["commitHash"] = self.commit_hash
        self.projects.append(project)

    def dump_to_json_file(self):
        with open(str(Settings.DIRECTORY_PROJECTS), "w") as f:
            f.write(json.dumps(self.projects, indent=4))

    def run_metrics(self):
        # Runs java method responsible for calculating OPN and IRN and extracting method invocations in order to calculate
        # CHM and CHD. Resorts to the projects.json written above as input.
        subprocess.call("mvn -Dtest=GenerateMetricsTest test",
                        cwd=f"{Settings.DIRECTORY}/symbolsolver/", shell=True)

        # output_fosci.csv is obtained from the execution of the previous metrics on the symbolsolver
        file_path = f"{Settings.DIRECTORY}/app/metrics/output_fosci.csv"
        chm = CHM.calculate(file_path)
        chd = CHD.calculate(file_path)
        ifn = IFN.calculate(file_path)

        path = f"{Settings.DIRECTORY}/data/services/{Settings.PROJECT_NAME}/{Settings.PROJECT_NAME}_{Settings.ID}"
        with open(path, "a+") as f:
            f.write(f"\nCHM: {chm}")
            f.write(f"\nCHD: {chd}")
            f.write(f"\nIFN: {ifn}")

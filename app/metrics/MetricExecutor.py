import re
import json
import subprocess
from Settings import Settings
import metrics.CHD as CHD
import metrics.CHM as CHM
import metrics.IFN as IFN
import metrics.SMQ as SMQ
import metrics.CMQ as CMQ


class MetricExecutor:

    def __init__(self):
        super().__init__()
        self.projects = []
        self.commit_hash = ''
        # self.commit_hash = str(subprocess.check_output(["git", "rev-parse", "--short", "HEAD"]))

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

        # subprocess.call(f"java -Dmetrics -cp symbolsolver-1.0.jar Main",
        # cwd=f"{Settings.DIRECTORY}/symbolsolver/target/", shell=True)

        output = subprocess.check_output(f"java -Dmetrics -Dproject={Settings.PROJECT_PATH} -cp symbolsolver-1.0.jar Main",
                                         cwd=f"{Settings.DIRECTORY}/symbolsolver/target/", shell=True)

        output = str(output).replace("b'", "").split("\\n")

        print("\n\n\n Executing Java's metric evaluation:")
        irn = 9999
        opn = 9999
        for line in output:
            irn_match = re.search(r'IRN Project: (\d*(\.\d*)?)', line)
            opn_match = re.search(r'OPN Project: (\d*(\.\d*)?)', line)

            if irn_match:
                irn = float(irn_match[1])

            if opn_match:
                opn = float(opn_match[1])

        # output_fosci.csv is obtained from the execution of the previous metrics on the symbolsolver
        file_path = f"{Settings.DIRECTORY}/app/metrics/output_fosci.csv"
        print(f"File path for metric calculation: {file_path}")
        chm = CHM.calculate(file_path)
        chd = CHD.calculate(file_path)
        ifn = IFN.calculate(file_path)
        smq, scoh, scop = SMQ.calculateWrapper()
        cmq, ccoh, ccop = CMQ.calculateWrapper()

        # path = f"{Settings.DIRECTORY}/data/services/{Settings.PROJECT_NAME}/{Settings.PROJECT_NAME}_{Settings.ID}_K{Settings.K_TOPICS}.csv"
        # with open(path, "a+") as f:
        #     f.write(f"\nCHM: {chm}")
        #     f.write(f"\nCHD: {chd}")
        #     f.write(f"\nIFN: {ifn}")
        #     f.write(f"\nSMQ: {smq}")
        #     f.write(f"\nCMQ: {cmq}")

        print(f"FINAL CHM : {chm}")
        print(f"FINAL CHD : {chd}")
        print(f"FINAL IFN : {ifn}")
        print(f"FINAL IRN : {irn}")
        print(f"FINAL OPN : {opn}")
        print(f"FINAL SMQ : {smq} {scoh} {scop}")
        print(f"FINAL CMQ : {cmq} {ccoh} {ccop}")

        return chm, chd, ifn, irn, opn, smq, scoh, scop, cmq, ccoh, ccop
from StringUtils import StringUtils
import re


class ProjectStats:

    def __init__(self, project_directory):
        self.project_directory = project_directory

    def is_valid_line(self, line):
        # Filter empty lines, comments, lines with {} only
        pattern = r'^\s*$|^[}{]|^\/{2}|^\/\*'
        return False if re.search(pattern, line) else True

    def lines_of_code(self):
        files_paths = StringUtils.search_java_files(self.project_directory)
        print(f"Files paths: {files_paths}")
        print(f"Files paths len: {len(files_paths)}")

        project_lines = 0
        for path in files_paths:
            with open(path, 'r') as f:
                line = f.readline()
                while line:
                    print(f"LINE: {line}")
                    line = f.readline()
                    # if self.is_valid_line(line):
                    project_lines += 1

        print(f"PROJECT LINES: {project_lines}")

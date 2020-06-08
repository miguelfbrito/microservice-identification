import os
import re

folders = {}
files = []
for i, j, k in os.walk('.'):
    if i in folders:
        folders[i].append(k)
    else:
        folders[i] = []
        folders[i].extend(k)

del folders['.']
# print(f"{i} - {j} - {k}")

files = []
for key, values in folders.items():
    for curr_file in values:
        files.append(f"{key}/{curr_file}")

N = 4
project_data = {}
for path in files:
    print(path)
    with open(path, 'r') as f:
        proj_name = re.findall(r'\/([a-zA-Z0-9-_]*)\/', path)[0]
        proj_id = re.findall(r'\/([a-zA-Z0-9-_]*)$', path)[0]
        k_topics = re.findall(r'K(\d*)', path)[0]

        proj = (proj_name, proj_id)
        for line in (f.readlines()[-N:]):
            match = re.findall(r'^(\w*): (\d*\.\d*)', line)

            for m in match:
                print(m[0] + "  " + m[1] + " " + str(proj_name))
                if proj in project_data:
                    project_data[proj].append(
                        (k_topics, m[0], m[1], proj_id))
                else:
                    project_data[proj] = [
                        (k_topics, m[0], m[1], proj_id)]


print("\n\n\nPROJECT DATA")
print(project_data)

with open('./results.csv', 'w') as f:
    f.write(f"project;k_topics;IRN; OPN; CHM; CHD;;ID\n\n")
    for project in project_data:
        data = project_data[project]
        head_line = f"{project[0]}"
        print(f"DATA {data}")
        line = f"{head_line};{data[0][0]}"
        for metric in data:
            line += f";{metric[2]}"
            if metric[1] == 'CHD':
                f.write(f"{line};;{project[1]}")

                line = f"{head_line};{metric[0]}"

        f.write("\n")

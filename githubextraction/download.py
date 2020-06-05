import subprocess

with open('./repos_data.txt', 'r') as f:
    line = f.readline()
    index = 0
    while line:
        full_name = line.split(',')[0]
        user, repo = full_name.split('/')
        command = f"git clone git@github.com:{user}/{repo}"
        print(f"Cmd {index}: {command}"
        line=f.readline()
        index += 1
        subprocess.call(command, shell=True)

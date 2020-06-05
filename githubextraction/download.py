import subprocess

with open('./repos_data.txt', 'r') as f:
    line = f.readline()
    index = 0
    while index < 200:
        full_name = line.split(',')[0]
        user, repo = full_name.split('/')
        command = f"git clone https://github.com/{user}/{repo}.git {user}__{repo}"
        print(f"Cmd {index}: {command}")
        subprocess.call(command, shell=True)
        line = f.readline()
        index += 1



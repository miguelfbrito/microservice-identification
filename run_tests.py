import subprocess

runs = 10
projects = [('spring-blog', 8), ('jpetstore', 5), ('monomusiccorp', 8),
            ('spring-petclinic', 4), ('jforum', 18), ('agilefant', 25)]

for i in range(30):
    for project in projects:
        command = f"python3 main.py -m {project[0]} -k {project[1]} > /dev/null"
        print(f"COMMAND {command}")
        subprocess.call(command, cwd="/home/mbrito/git/thesis/app", shell=True)

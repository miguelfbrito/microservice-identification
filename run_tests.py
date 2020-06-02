import subprocess

runs = 10
projects = [('spring-blog', 8), ]

for j in range(10):
    for i in range(3,10):
        command = f"python3 main.py -p spring-blog -k {i} > /dev/null"
        print(f"COMMAND {command}")
        subprocess.call(command, cwd="/home/mbrito/git/thesis/app", shell=True)

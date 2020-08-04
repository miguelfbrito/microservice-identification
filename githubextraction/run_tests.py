import subprocess
import random
import os.path
from os import path

# user/repo, classes_count, controllers_count
with open('./merged_data_cortado_3.csv', 'r') as f:
    line = f.readline()
    count = 0
    while line:
        user_repo, class_count, controller_count,_,_,_,_,_,_,_,_ = line.split(',')
        line = f.readline()
        controller_count = int(controller_count)
        
        name = user_repo.replace('/', '__')
        if path.exists(f"/home/mbrito/git/thesis-web-applications/monoliths/{name}"):
            command = f"python3 /home/mbrito/git/thesis/app/main.py -p {name} -m" # > /dev/null
            print(f"\n\nCMD: {command}")
            subprocess.call(command, cwd="/home/mbrito/git/thesis/app", shell=True)
        else:
            print(f"Path for {name} doesn't exist")

        count += 1

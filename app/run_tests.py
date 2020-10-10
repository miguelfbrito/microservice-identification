import subprocess
import random
import os.path
import time
from os import path

# user/repo, classes_count, controllers_count
# with open('./merged_data_cortado_3.csv', 'r') as f:
with open('./final_project_data.csv', 'r') as f:
    line = f.readline()
    count = 0
    while line:
        #user_repo, class_count, controller_count,_,_,_,_,_,_,_,_ = line.split(',')
        user_repo, class_count, controller_count = line.split(',')
        line = f.readline()
        controller_count = int(controller_count)
        class_count = int(class_count)

        k = 10 
        if class_count < 50:
            k = 10
        elif class_count < 100:
            k = 13
        elif class_count < 200:
            k = 17
        elif class_count < 500:
            k = 22
        elif class_count < 1000:
            k = 26
        else:
            k = 32

        
        name = user_repo.replace('/', '__')
        if path.exists(f"/home/mbrito/git/thesis-web-applications/monoliths/{name}"):
            command = f"python3 main.py -p /home/mbrito/git/thesis-web-applications/monoliths/{name} -m > /dev/null"  # > /dev/null
            print(f"\n\nCMD: {command}")
            t0 = time.time()
            subprocess.call(command, cwd="/home/mbrito/git/thesis/app", shell=True)
            time_elapsed = time.time() - t0

            with open('./time_output', 'a+') as ftime:
                ftime.write(f"{user_repo},{class_count},{controller_count},{time_elapsed}\n")
        else:
            print(f"Path for {name} doesn't exist")

        count += 1

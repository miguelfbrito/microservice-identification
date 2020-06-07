import subprocess

runs = 3 
# user/repo, classes_count, controllers_count
with open('./final_projects', 'r') as f:
    line = f.readline()
    while line:
        user_repo, class_count, controller_count = line.split(',')
        line = f.readline()
        k = 0
        controller_count = int(controller_count)
        if controller_count < 25:
            k = 10
        elif controller_count < 100:
            k = 15
        elif controller_count < 200:
            k = 20
        else:
            k = 25
        
        command = f"python3 /home/mbrito/git/thesis/app/main.py -p {user_repo.replace('/', '__')} -k {k} -m > /dev/null"
        print(f"\n\nCMD: {command}")
        subprocess.call(command, cwd="/home/mbrito/git/thesis/app", shell=True)

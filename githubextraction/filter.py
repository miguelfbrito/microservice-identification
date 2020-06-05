import os
import time
import re

JAVA_MIN_COUNT = 100

def walkdir(dirname):
    src_count = 0
    java_count = 0
    controller_count = 0
    curr_dir = ''
    curr_proj = ''
    projects = set()

    for cur, directories, files in os.walk(dirname):
        directories[:] = [d for d in directories if d not in {'target', '.git'}]
        pref = ''
        head, tail = os.path.split(cur)
        #time.sleep(0.5)
        #print(f"CUR: {cur}")
        
        split = cur.split('/')
        # New project 
        if len(split) == 2:
            if src_count == 1 and java_count >= JAVA_MIN_COUNT and controller_count > 10:
                print(f"Valid repo: {curr_proj} classes: {java_count} controllers:  {controller_count}")
                projects.add((curr_proj, java_count, controller_count))

            # Reset for next project
            src_count = 0
            java_count = 0
            controller_count = 0
            curr_proj = split[1]
        
        curr_dir = split[len(split)-1]
        if curr_dir == 'src':
            src_count += 1
            #directories[:] = []

        for file in files:
            file_split = file.split('.')
            if len(file_split) == 2:
                if file_split[1].lower() == 'java':
                    java_count += 1
            if re.search(r'controller|action', file_split[0].lower()):
                controller_count += 1

    with open('./final_projects', 'a+') as fp:
        for proj in projects:
            repo = proj[0].replace('__', '/')
            fp.write(f"{repo},{proj[1]},{proj[2]}\n")

walkdir('.')

import subprocess

runs = 3 
# (project, k-min, k-max)
projects = [
        ('agilefant', 4,25),
        ('flight-booking-system',2,4),
        ('greenhouse',3,20),
        ('jforum',4,20),
        ('librarian',3,8),
        ('library-application',3,5),
        ('monomusiccorp',3,7),
        ('online-banking',3,8),
        ('realworld',4,15),
        ('shopping',3,9),
        ('spring-blog',3,9),
        ('spring-petclinic',3,9),
        ('sunrise',4,28),
        ('tntconcept',4,25)
    ]

for i in range(1,40):
    for proj in projects:
        for val in range(proj[1], proj[2]):
            command = f"python3 main.py -p {proj[0]} -k {val} -m > /dev/null"
            print(f"\n\n\nCMD: {command}")
            subprocess.call(command, cwd="/home/mbrito/git/thesis/app", shell=True)

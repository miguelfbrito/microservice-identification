# Microservice Identification

A shell-based tool that resorts to Topic Modelling and Community detection techniques in order to identify microservices from a project with a monolithic architecture. 
Currently it only supports Java projects.

## Setup
1. Clone the repo
2. Give execution permitions to setup script `chmod u+x setup.sh`
3. Execute `setup.sh`


## Usage
All the interaction with the tool is done through the python script `main.py` located in `/app/` directory.

```
main.py [-h] --project PROJECT [--stop_words STOP_WORDS]
               [--k_topics K_TOPICS] [--resolution RESOLUTION] [--metrics]
               [--draw] [--lda-plotting]

optional arguments:
  -h, --help            show this help message and exit
  --project PROJECT, -p PROJECT
                        Project path
  --stop_words STOP_WORDS, -s STOP_WORDS
                        Path to stopwords file
  --k_topics K_TOPICS, -k K_TOPICS
                        Number of topics for given project
  --resolution RESOLUTION, -r RESOLUTION
                        Resolution number parameter in Louvain community
                        detection. A range between 0.3 and 1 is advised. A
                        smaller resolution will identify smaller communities (smaller microservices)
                        and vice versa. By default the whole range is tested
                        and communities for each community saved.
  --metrics, -m         Execute metrics for a given project name after normal
                        parsing and execution (At the current time it does NOT work independently
                        from the identification process)
  --draw, -d            Enable plotting of graphs
  --lda-plotting, -l    Enable plotting of LDA topics which might help identifying the appropriate number of topics
```

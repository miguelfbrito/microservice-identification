import re
import json
import time
import requests
import subprocess

from bs4 import BeautifulSoup

# TODO: REVOKE TOKEN BEFORE GOING PUBLIC
ACCESS_TOKEN = "e0754d7bf67f3bb1da2e77b881740627b442aefb"

def find_repositories_containing_keyword(keyword, language='java'):
    for min_size in range(1000, 100000, 250):
        max_size = min_size + 250
        # 35 is the max number of pages for the limit of 1000 results for a search established by GitHub
        for i in range(1, 35):
            url = f"https://api.github.com/search/code?q={keyword}+language:{language}+size:{min_size}..{max_size}&access_token={ACCESS_TOKEN}&page={i}"
            req = requests.get(url)
            print(f"Requesting {url}")
            if req.ok:
                data = req.json()
                print(f"Request status code: {req.status_code}, page {i}")
                if len(data['items']) == 0:
                    break
                yield req.json()
            else:
                break


def get_repos():
    repositories = {}
    for data in find_repositories_containing_keyword('RequestMapping+Controller'):
        items = data['items']
        repositories = {item['repository']['full_name'] for item in items}
        time.sleep(3)

        with open("repos_new.txt", "a+") as f:
            for key in repositories:
                print(f"Found repo: {key}")
                f.write(key+"\n")

    return repositories


def download_repo(repo_full_name):
    user, repo = repo_full_name.split("/")
    url = f"git@github.com:{user}/{repo}.git"
    command = f"git clone {url}"
    subprocess.call(command, shell=True)


def get_repo_stars(repo_full_name):
    user, repo = repo_full_name.split("/")
    url = f"https://github.com/{user}/{repo}"
    req = requests.get(url)
    html = req.text

    bs = BeautifulSoup(html)
    stars_text = bs.findAll('a', {'class': 'social-count'})[1].contents[0]
    stars = stars_text.strip()
    if '.' in stars:
        stars = stars.replace('k', '00')
    else:
        stars = stars.replace('k', '000')
    stars = stars.replace('.', '')
    print(f"Stars {stars}")
    return stars


def get_stars_all_repos():
    data = []
    with open('./repos.txt', 'r') as f:
        line = f.readline()
        while line:
            line = line.strip()
            print(f"Visiting {line}")
            stars = get_repo_stars(line)
            data.append((line, stars))

            with open('./repos_stars.txt', 'a+') as fp:
                fp.write(f"{line}, {stars}\n")

            line = f.readline()

    sorted(data, key=lambda x: x[1], reverse=True)
    for i in data:
        print(i)


def get_commit_count(user, repo): 

    headers = {"Authorization": f"token {ACCESS_TOKEN}"} 
    query = f"""
        {{
            repository(owner: "{user}", name: "{repo}") {{
                name
                refs(first:100, refPrefix: "refs/heads/", query:"master") {{
                    edges {{
                        node {{
                            name
                            target {{
                                ... on Commit {{
                                    history(first: 0) {{
                                        totalCount
                                    }}
                                }}
                            }}
                        }}
                    }}
                }}
            }}
        }}"""

    request = requests.post("https://api.github.com/graphql", json={"query":query}, headers=headers)
    commit_count = 0
    if request.ok:
        data = json.loads(request.text)
        commit_count = data['data']['repository']['refs']['edges'][0]['node']['target']['history']['totalCount']
        print(f"Total Commits: {commit_count}")
    else:
        print("Failed to request commit data")

    return commit_count


def get_info_repo(repo_full_name):
    repository = {}
    user, repo = repo_full_name.split("/")
    r = requests.get(
        f"https://api.github.com/repos/{user}/{repo}?access_token={ACCESS_TOKEN}")
    if(r.ok):
        repo_info = json.loads(r.text or r.content)
        repository['size'] = repo_info['size']
        repository['forks'] = repo_info['forks_count']
        repository['stars'] = repo_info['stargazers_count']
        repository['open_issues'] = repo_info['open_issues']
        repository['subscribers'] = repo_info['subscribers_count']
        repository['is_fork'] = True if repo_info['fork'] == 'true' else False
        repository['language'] = repo_info['language']

    repository['commit_count'] = get_commit_count(user, repo) 

    time.sleep(3)

    req_contributors = requests.get(f"https://api.github.com/repos/{user}/{repo}/contributors?access_token={ACCESS_TOKEN}")
    if (req_contributors.ok):
        repository['contributors'] = len(json.loads(req_contributors.text or req_contributors.content))

    return repository


def filter_found_projects(file_path):
    ignore_words = {'release', 'framework',
                    'learn', 'source', 'spring', 'study', 'demo', 'test', 'practice', 'practise'}
    filtered_projects = set()
    with open(file_path, 'r') as f:
        line = f.readline()
        while line:
            print(f"Filtering {line}")
            line = line.strip()
            ignore = False
            for word in ignore_words:
                if word in line.lower():
                    ignore = True

            if not ignore:
                filtered_projects.add(line)
            line = f.readline()

    # First filter
    with open(f"{file_path}_filtered", 'w+') as f:
        for project in filtered_projects:
            f.write(f"{project}\n")


def get_repos_info(file_path):
    with open(f"{file_path}_filtered", 'r') as f:
        line = f.readline()
        while line:
            print(f"Filtering size {line}")
            line = line.strip()
            info_repo = get_info_repo(line)
            time.sleep(5)
            print(f"{line} -> {info_repo}")

            with open(f"./repos_data.txt", 'a+') as fp:
                try:
                    write_line = f"{line}, {info_repo['size']}, {info_repo['stars']}, {info_repo['forks']}, {info_repo['contributors']}, {info_repo['is_fork']}, {info_repo['language']}, {info_repo['subscribers']}, {info_repo['commit_count']}, {info_repo['open_issues']}\n"
                    fp.write(write_line)
                except KeyError:
                    print(f"Failed to fetch some data, ignoring this repo")
            




            line = f.readline()

if __name__ == "__main__":
    #repos = get_repos()
    filter_found_projects('./repos.txt')
    get_repos_info('./repos.txt')


#repositories = {}
# for file in json_files:
#    with open("./applications_1.json", 'r') as f:
#        data = json.load(f)
#        items = data['items']
#
#        repositories = {item['repository']['full_name'] for item in items}
#
# print(repositories)

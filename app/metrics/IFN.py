import sys


def calculate(api_file_name):
    services = {}  # services: classes
    with open(api_file_name, 'r') as f:
        line = f.readline()
        while line:
            data = line.split(',')
            print(data[1])
            if data[0] in services:
                services[data[0]].add(data[1])
            else:
                services[data[0]] = set()
                services[data[0]].add(data[1])
            line = f.readline()

    ifn = 0
    for _, interfaces in services.items():
        ifn += len(interfaces)

    return ifn / len(services.keys())


if __name__ == "__main__":
    api_file_name = sys.argv[1]
    calculate(api_file_name)

#!/bin/bash
path="$PWD"
sudo apt update -y

# Install python and its dependencies
sudo apt install software-properties-common -y
sudo add-apt-repository ppa:deadsnakes/ppa -y
sudo apt install python3.7 -y
sudo apt install python3-pip -y
cd app
pip3 install -r requirements.txt
sudo apt-get install python3-sklearn python3-sklearn-lib python3-sklearn-doc -y

# Install Mallet
export JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF8
sudo apt install ant -y
git clone https://github.com/mimno/Mallet.git ~/.mallet
cd ~/.mallet
ant

cd "$path"
# Install Maven and build 
cd symbolsolver
sudo apt install maven -y
mvn package -DskipTests


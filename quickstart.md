# How to quick start

## Introduction

This guide provides two ways for CYPHER quickstart:
- Set up a FullNode using the official tools: providing a wealth of configurable parameters to startup a FullNode
- Set up a complete private network for Cypher development 

## Dependencies

### Docker

Please download and install the latest Docker from Docker official website:
* Docker Installation for [Mac](https://docs.docker.com/docker-for-mac/install/)
* Docker Installation for [Windows](https://docs.docker.com/docker-for-windows/install/)   

## Quickstart based on official tools

### Build the docker image from source

#### Clone the java-cypher repo

Clone the cypher-node repo from github and enter the directory `cypher-node`:
```
git clone https://github.com/Karthigasrini/cypher-node.git
cd cypher-node
```

#### Build the docker image

Use below command to start the build:
```
docker build -t cypher-node .
```

### Run the container

You can run the command below to start the java-cypher:
```
docker run -e PRIVATE_KEY="your_private_key_here" -d your_image_id
```

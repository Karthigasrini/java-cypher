FROM tronprotocol/centos7:0.2

RUN set -o errexit -o nounset \
#  Download and build java-cypher
    && echo "git clone" \
    && git clone https://github.com/Karthigasrini/cypher-node.git \
    && cd cypher-node \
    && gradle build

# Change work directory
WORKDIR /cypher-node

# open port 18888
EXPOSE 18888


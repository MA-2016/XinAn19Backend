# base image
FROM ubuntu:16.04

COPY elasticsearch-7.1.0-amd64.deb /home/elasticsearch-7.1.0-amd64.deb
COPY kibana-7.1.0-linux-x86_64.tar.gz /home/kibana-7.1.0-linux-x86_64.tar.gz
COPY filebeat-6.7.2-amd64.deb /home/filebeat-6.7.2-amd64.deb

RUN apt-get update \
    # && apt install -y net-tools \
    # && apt install -y iputils-ping \
    && cd /home \
    && dpkg -i elasticsearch-7.1.0-amd64.deb \
    # && /etc/init.d/elasticsearch start \
    && cd /home \
    && tar xzvf kibana-7.1.0-linux-x86_64.tar.gz \
    # && cd kibana-7.1.0-linux-x86_64/ \
    # && ./bin/kibana & \
    && cd /home \
    && dpkg -i filebeat-6.7.2-amd64.deb
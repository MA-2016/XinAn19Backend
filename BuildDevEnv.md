# Build Development Environment

## Install Docker for your system

References:

* https://www.docker.com
* https://www.runoob.com/docker/docker-tutorial.html

## Build in Docker

### 1.Create a container with Filebeat image

Reference:

* https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-configuration.html

```
> docker pull elastic/filebeat:7.0.1
```

Remeber to run ```docker ps -a``` to check whether kibana is running.

### 2.Create a container with Logstash image

Reference:

* https://www.elastic.co/guide/en/logstash/current/docker-config.html#docker-bind-mount-settings
* https://www.elastic.co/guide/en/beats/filebeat/current/logstash-output.html

```
> docker pull elastic/logstash:7.0.1

```

### Referencces

* [Filebeat Configuration](https://blog.csdn.net/shunqixing/article/details/80401689)
* [logstash配合filebeat监控tomcat日志](https://www.cnblogs.com/guochunyi/p/6130962.html)
* [Logstash使用grok过滤nginx日志（二）](https://www.cnblogs.com/Orgliny/p/5592186.html)

## Others

### Create a container with Kibana image

Reference: https://hub.docker.com/_/kibana

```
> docker network create xnet
> docker pull kibana:7.0.1
> docker run -d --name kibana-7.0.1 --net xnet -h kibana-host -p 5601:5601 kibana:7.0.1
```


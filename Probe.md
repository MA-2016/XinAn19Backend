全局架构图：未贴

# 探针实现

架构图：

![](http://luncert.cn:9080/StaticFileService/static-file/XinAn.Image.probe1)

这个图需要调整，Kafka要放在Logstash前面。

## 1.原始数据采集

我们主要采集两类数据，一类是输出到文件系统的日志数据，一类是网络监听抓取到的数据，分别基于Filebeat和Packetbeat实现。接下来详细介绍Filebeat和Packetbeat。

### Filebeat

Filebeat是一个轻量型日志采集器，支持转发和汇总日志和文件，内置多种模块（auditd、Apache、NGINX、System、MySQL等等），可针对常见格式的日志大大简化收集、解析和可视化过程。Filebeat使用Golang进行开发，性能优秀，运行稳定。

Filebeat有两个主要组件组成：查找器prospector和harvester。这些组件一起工作来读取文件并将事件数据发送到您指定的输出。

* 启动Filebeat时它会启动一个或多个prospector，查看配置中指定的日志文件本地路径。
* 对于prospector所在的每个日志文件，prospector 启动harvester。 
* 每个harvester都会为新内容读取单个日志文件，并将新日志数据发送到libbeat。
* libbeat是Beats系列产品的核心模块，负责将聚合事件并将聚合数据发送到配置的Filebeat输出。

![](https://upload-images.jianshu.io/upload_images/3763264-8c83a34f568d1a67.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/940/format/webp)

### Packetbeat

Packetbeat是一个实时网络包分析器，可以监听服务器网络通信，解析应用层协议，关联请求报文与响应报文并生成一次事务数据（一个请求和对应的响应我们成为一次事务），从每次通信中提取并记录感兴趣的字段。

Packetbeat会捕获到指定端口的数据包，然后解析成JSON，Packetbeat本身提供了常用协议的解析器，包括ICMP、DHCP、DNS、HTTP、MySQL、Redis等等，也提供了编程接口支持拓展协议开发。

### 关于部署到设备

由于安装Filebeat和Packetbeat后还有一系列配置要做，包括要采集的日志文件的路径、要监听的网络端口号及数据包解析器，（开始吹）为了在满足用户基本需求的基础上，提供数据采集自定义，我们提供了一个web应用允许用户通过交互的方式配置配置项，在完成配置后该应用能够自动根据用户自定义配置将Filebeat和Packetbeat打包成一个Docker镜像，并作为一个有限期的web资源提供给用户，用户只需要在设备上装好Docker，直接通过Docker以url的方式拉取并运行这个web上的镜像即可完成部署。十分便捷。

## 2.数据缓冲

受管理集群中的每一个设备都会安装上Filebeat和Packetbeat以进行数据采集。集群设备多，在网络流量高峰期产生的网络数据和日志数据的量可能非常庞大，这种情况如果我们的系统消费能力不足，可能会导致系统拒绝服务（出现类似Dos攻击的情况）。因此我们引入一个高性能的消息中间件对数据进行缓冲——Kafka。

Kafka是一个分布式消息队列，具有高性能、持久化、多副本备份、横向拓展能力，是同类产品中的佼佼者。我们使用Kafka进行削峰，即通过将消息缓存排队降低并发量，适配消费者的消费能力。

## 3.数据转换

从Kafka输出的原始数据还不能直接使用，因为这些数据来源多种多样，需要从每一类数据中提取出关键信息，按统一的格式输出。示例的输出数据为：
```
{
    "id": "ce2ce281-236b-4254-bc8f-ef1070f65ed7", // 生成的全局统一的事件uuid
    "timestamp": 15584716780424L, // 事件发生时间
    "attrs": { // 从事件数据中提取出的键值对类型的数据
        "srcAddr": "192.168.1.13",
        "srcPort": 51236,
        "tarAddr": "192.168.1.3",
        “tarPort": 80
    },
    "tuples": [ // 从事件数据中提取出的内容偏自然语言的数据，有序，将用于知识推导
        "tomcat",
        "HTTP/1.1",
        "connection closed"
    ]
}
```
这个格式的数据即是将用于匹配（我们可以换个说法，叫知识退到）的数据了。

我们使用Logstash进行数据转换。Logstash是一个开源数据收集引擎，具有实时管道功能。Logstash可以动态地将来自不同数据源的数据统一起来，并将数据标准化后输出。

Logstash的工作原理是：首先为Logstash创建一条工作管道，数据（从Kafka发过来）输入管道后，经过Grok过滤器插件被转换为特定格式的数据，然后进行持久化，并输出（输出目的地有配置文件决定）。

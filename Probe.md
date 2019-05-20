# 探针技术原理

架构图：

![](https://ai.uppfind.com/StaticFileService/static-file/XinAn.Image.probe1)
https://ai.uppfind.com/StaticFileService/static-file/XinAn.Image.probe1

## 组件介绍

### Beats：收集数据，转换结构，传输数据

#### Filebeat

> Filebeat主要是如何配置，这个已经解决了，接下来就到真实的场景中跑一下，看看真实的数据是什么样的，然后就接入logstash进行提词，然后把词拿去和知识图谱进行匹配。
>
> 1.Filebeat的数据格式； 2.logstash过滤转换后的数据格式； 3.匹配算法

##### 1.什么是Filebeat？

Filebeat是一个轻量型日志采集器，支持转发和汇总日志和文件，内置多种模块（auditd、Apache、NGINX、System、MySQL等等），可针对常见格式的日志大大简化收集、解析和可视化过程。Filebeat使用Golang进行开发，性能优秀，运行稳定。

##### 2.工作原理

Filebeat有两个主要组件组成：查找器prospector和harvester。这些组件一起工作来读取文件并将事件数据发送到您指定的输出。

* 启动Filebeat时它会启动一个或多个prospector，查看配置中指定的日志文件本地路径。
* 对于prospector所在的每个日志文件，prospector 启动harvester。 
* 每个harvester都会为新内容读取单个日志文件，并将新日志数据发送到libbeat。
* libbeat是Beats系列产品的核心模块，负责将聚合事件并将聚合数据发送到配置的Filebeat输出。

![](https://upload-images.jianshu.io/upload_images/3763264-8c83a34f568d1a67.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/940/format/webp)

注：Filebeat只能读取本地文件，不支持连接到远程主机来读取存储的文件或日志。

#### Packetbeat

Packetbeat是一个实时网络包分析器，可以监听服务器网络通信，解析应用层协议（ICMP、DHCP、DNS、HTTP、MySQL、Redis等等），关联请求报文与响应报文并生成一次事务数据（一个请求和对应的响应我们成为一次事务），从每次通信中提取并记录感兴趣的字段。

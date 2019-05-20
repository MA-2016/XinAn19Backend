# 探针技术原理

架构图：

![主要架构图](http://luncert.cn:9080/StaticFileService/static-file/XinAn.Image.probe1)

## 组件介绍

### Beats：收集数据，转换结构，传输数据

#### Filebeat

> Filebeat主要是如何配置，这个已经解决了，接下来就到真实的场景中跑一下，看看真实的数据是什么样的，然后就接入logstash进行提词，然后把词拿去和知识图谱进行匹配。
>
> 1.Filebeat的数据格式； 2.logstash过滤转换后的数据格式； 3.匹配算法

#### Packetbeat

Packetbeat是一个实时网络包分析器，可以监听服务器网络通信，解析应用层协议（ICMP、DHCP、DNS、HTTP、MySQL、Redis等等），关联请求报文与响应报文并生成一次事务数据（一个请求和对应的响应我们成为一次事务），从每次通信中提取并记录感兴趣的字段。

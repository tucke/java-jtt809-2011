# java-jtt809-2011

#### 介绍
JT / T809-2011 Java版本

不依赖spring、不依赖springboot，拒绝万物皆可spring ！我不是抵触spring，因为项目引入spring会让项目膨胀很大，spring虽然很方便，但是为了一点点小方便，似乎十分不划算！而且作为一个优秀的CV程序员，要学会自己写代码

#### 软件架构
后续补充


#### 安装教程

没啥特别之处，按照普通项目运行就行
项目入口可以查看 pom 文件 properties 的 start-class 配置

#### 使用说明

1.  GnssCenterService 管理下级平台的一些认证啥的，需要自己实现。甚至你自己去写死都行
2.  所有关于业务的协议处理都在 org.tucke.jtt809.handler.protocol 包下，只需根据自己使用的数据库实现数据保存即可。尽量避免使用一个文件，也尽量避免文件过多，建议将类型相同的协议放在一起（如连接类的协议都放在 org.tucke.jtt809.handler.protocol.connect.ConnectProtocol ）
3.  所有的内层包解析建议都放在 org.tucke.jtt809.packet 包下的消息实体类中，将消息实体和消息解码编码放在一起，方便代码管理和阅读，省得来回切换目录，眼花缭乱

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request

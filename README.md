## 项目启动

1. ### 克隆项目
    

```Bash
git clone https://github.com/LL1105/backend.git
```

2. ### 安装运行环境
    

#### 2.1 安装JDK

##### 2.1.1 打开浏览器，访问[Oracle官网](https://www.oracle.com/jp/java/technologies/downloads/)，找到JDK8版本，选择和电脑匹配的操作系统的jdk版本(Linux/Windows/Macos)进行下载。

##### 2.1.2双击jdk安装文件，根据向导依次点击下一步即可安装完毕。

#### 2.2 安装maven

##### 2.2.1 打开浏览器，访问[Maven官网](https://maven.apache.org/download.cgi)，下载Maven的压缩包文件。

##### 2.2.2 解压下载的压缩包到指定的文件夹。

3. ### 编译项目
    

```Bash
cd backend
mvn clean install
```

4. ### 按顺序启动各个模块
    

```Bash
java -jar data-service/data-service-server/target/data-service.jar
java -jar talentrank-service/talentrank-service-server/target/talentrank-service.jar
java -jar nation-service/nation-service-server/target/nation-service.jar
java -jar user-service/user-service-server/target/user-service.jar
java -jar domain-service/domain-service-server/target/domain-service.jar
java -jar api-service/target/api-service.jar
java -jar gateway-service/target/gateway-service.jar
```

[架构设计文档](https://github.com/LL1105/Gitgle/blob/master/docs/架构设计文档.pdf)

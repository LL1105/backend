## 运行项目

1. ### 克隆项目
    

```Bash
git clone https://github.com/LL1105/backend.git
```

2. ### 编译项目
    

```Bash
cd backend
mvn clean install
```

3. ### 按顺序启动各个模块
    

```Bash
java -jar data-service/data-service-server/target/data-service.jar
java -jar talentrank-service/talentrank-service-server/target/talentrank-service.jar
java -jar nation-service/nation-service-server/target/nation-service.jar
java -jar user-service/user-service-server/target/user-service.jar
java -jar domain-service/domain-service-server/target/domain-service.jar
java -jar api-service/target/api-service.jar
java -jar gateway-service/target/gateway-service.jar
```

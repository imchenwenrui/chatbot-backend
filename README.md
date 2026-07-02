# Chatbot Backend

基于 Spring Boot 2.7.x + JDK 17 的聊天机器人后端项目。

## 技术栈
- Spring Boot 2.7.18
- JDK 17
- Maven

## 运行

```bash
# 编译
mvn clean compile

# 启动
mvn spring-boot:run

# 打包
mvn clean package
java -jar target/chatbot-backend-0.0.1-SNAPSHOT.jar
```

服务默认监听 8080 端口。

## 接口示例

```
GET /api/chat/hello
```

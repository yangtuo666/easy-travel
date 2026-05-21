# 阶段一：使用 Maven 镜像进行项目构建
FROM maven:3.8.1-openjdk-17-slim as builder
# 解决容器时区与真实时间相差8小时的问题
RUN ln -snf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo Asia/Shanghai > /etc/timezone
WORKDIR /app
COPY pom.xml .
COPY src ./src
# 跳过测试直接打包
RUN mvn package -DskipTests

# 阶段二：运行环境
FROM eclipse-temurin:17-jre
WORKDIR /app
# 从构建阶段复制打好的 jar 包过来
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
# 启动 jar 包，并指定生产环境配置
ENTRYPOINT ["java","-jar","app.jar","--spring.profiles.active=prod"]

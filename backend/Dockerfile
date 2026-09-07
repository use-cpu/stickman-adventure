# syntax=docker/dockerfile:1
# ---------- 阶段 1:用 Maven 构建 jar ----------
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
# 先单独拷 pom,利用层缓存加速依赖下载
COPY pom.xml .
RUN mvn -q dependency:go-offline
# 再拷源码(注意 src 目录)
COPY src ./src
# 打包,跳过测试(镜像构建期不连数据库,测试可能失败)
RUN mvn -q clean package -DskipTests

# ---------- 阶段 2:运行时 jdk17,体积小 ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

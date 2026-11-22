# Stage 1: Build bằng Maven & JDK 21
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace

ARG MODULE_PATH

# Copy toàn bộ source code Monorepo và build module được chỉ định cùng các module phụ thuộc (-am)
COPY . .
RUN mvn clean package -pl ${MODULE_PATH} -am -DskipTests

# Stage 2: Chạy bằng JRE 21 Alpine siêu nhẹ
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

ARG MODULE_PATH
COPY --from=builder /workspace/${MODULE_PATH}/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseZGC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
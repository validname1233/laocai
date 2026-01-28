FROM eclipse-temurin:25

WORKDIR /
ENV TZ=Asia/Shanghai

COPY app.jar /app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]

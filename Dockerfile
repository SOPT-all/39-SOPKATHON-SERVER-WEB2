FROM eclipse-temurin:21-jre

RUN groupadd --system app && useradd --system --gid app --no-create-home app

WORKDIR /app
COPY build/libs/*.jar app.jar

ENV TZ=Asia/Seoul
ENV JAVA_OPTS="-Xms256m -Xmx512m"
EXPOSE 8080

USER app

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]

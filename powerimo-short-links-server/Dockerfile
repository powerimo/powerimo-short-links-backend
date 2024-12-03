FROM amazoncorretto:17
ARG APP_FILE=target/*.jar

ENV JAVA_TOOL_OPTS="-Xms128m -Xmx128m"
ENV SERVER_PORT=8080

RUN mkdir /app

COPY ${APP_FILE} /app/powerimo-short-links-server.jar

EXPOSE 8080/tcp

WORKDIR "/app"

CMD ["java", "-jar", "powerimo-short-links-server.jar"]

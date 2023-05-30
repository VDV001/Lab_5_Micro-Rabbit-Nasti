FROM openjdk:17

ARG JAR_FILE=/target/client-1.jar

COPY ${JAR_FILE} client-1.jar

ENTRYPOINT ["java", "-jar", "client-1.jar"]


FROM openjdk:17

ARG JAR_FILE=/build/libs/*.jar

COPY ${JAR_FILE} pt.jar

ENTRYPOINT ["java","-jar","pt.jar"]

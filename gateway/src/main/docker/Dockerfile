FROM eclipse-temurin:21-alpine

USER root

COPY gateway/target/*.jar application.jar

ENTRYPOINT ["java","-jar","/application.jar"]
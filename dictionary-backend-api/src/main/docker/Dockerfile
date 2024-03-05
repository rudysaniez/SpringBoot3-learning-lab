FROM eclipse-temurin:21-alpine

USER root

COPY dictionary-backend-api/target/*.jar application.jar

ENTRYPOINT ["java","-jar","/application.jar"]
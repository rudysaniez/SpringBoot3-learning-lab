FROM eclipse-temurin:21-alpine

USER root

COPY eureka-server/target/*.jar application.jar

ENTRYPOINT ["java","-jar","/application.jar"]
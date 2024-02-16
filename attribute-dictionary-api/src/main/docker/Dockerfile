FROM eclipse-temurin:21-alpine

USER root

COPY attribute-dictionary-api/target/*.jar application.jar

RUN mkdir /etc/attribute-dictionary-api
WORKDIR /etc/api/
VOLUME [ "/etc/api/" ]

ENTRYPOINT ["java","-jar","/application.jar"]
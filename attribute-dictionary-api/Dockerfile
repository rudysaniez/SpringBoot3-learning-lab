FROM eclipse-temurin:21-alpine

USER root

COPY target/*.jar application.jar

RUN mkdir /etc/attribute-dictionary-api
WORKDIR /etc/me/
VOLUME [ "/etc/me/" ]

ENTRYPOINT ["java","-jar","/application.jar"]
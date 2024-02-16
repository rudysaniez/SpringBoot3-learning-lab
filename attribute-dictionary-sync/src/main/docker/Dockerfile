FROM eclipse-temurin:21-alpine

USER root

COPY attribute-dictionary-sync/target/*.jar application.jar

RUN mkdir /etc/attribute-dictionary-sync
WORKDIR /etc/sync/
VOLUME [ "/etc/sync/" ]

ENTRYPOINT ["java","-jar","/application.jar"]
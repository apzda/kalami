FROM apzda/eclipse-temurin:23-jre-alpine AS builder

ARG SERVICE_JAR

COPY ${SERVICE_JAR} /opt/app/application.jar

RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

FROM apzda/eclipse-temurin:23-jre-alpine

ARG SERVICE_NAME
ARG SERVICE_VER

LABEL "co.elastic.logs/enabled"="true"\
      "co.elastic.logs/multiline.type"="pattern"\
      "co.elastic.logs/multiline.pattern"="^\d{4}-\d{2}-\d{2}(\s|T)(\d{2}:){2}\d{2}.+"\
      "co.elastic.logs/multiline.negate"="true"\
      "co.elastic.logs/multiline.match"="after"\
      "co.elastic.logs/processors.0.add_fields.target"="service"\
      "co.elastic.logs/processors.0.add_fields.fields.name"="${SERVICE_NAME}"\
      "co.elastic.logs/processors.1.add_fields.target"="data_stream"\
      "co.elastic.logs/processors.1.add_fields.fields.dataset"="${SERVICE_NAME}"

ENV JAVA_OPTS="-Xms512M -Xmx512M"\
    APP_OPTS="" \
    SERVICE_NAME="${SERVICE_NAME}"\
    SERVICE_VER="${SERVICE_VER}"\
    SERVER_PORT=8080

COPY --from=builder /opt/app/bin/ ./bin/
COPY --from=builder /opt/app/extracted/dependencies/ ./${SERVICE_NAME}/
COPY --from=builder /opt/app/extracted/company-dependencies/ ./${SERVICE_NAME}/
COPY --from=builder /opt/app/extracted/spring-boot-loader/ ./${SERVICE_NAME}/
COPY --from=builder /opt/app/extracted/snapshot-dependencies/ ./${SERVICE_NAME}/
COPY --from=builder /opt/app/extracted/application/ ./${SERVICE_NAME}/

CMD ["-mode","launcher"]

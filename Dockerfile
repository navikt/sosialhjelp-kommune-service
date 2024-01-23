FROM gcr.io/distroless/java21

COPY build/libs/sosialhjelp-kommune-service-all.jar /app/sosialhjelp-kommune-service-all.jar

ENV JAVA_OPTS="-Dlogback.configurationFile=logback.xml"

WORKDIR /app

USER nonroot

CMD ["sosialhjelp-kommune-service-all.jar"]

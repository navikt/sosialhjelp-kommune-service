FROM gcr.io/distroless/java17

COPY build/libs/sosialhjelp-kommune-service-all.jar /app/sosialhjelp-kommune-service-all.jar

WORKDIR /app

USER nonroot

CMD ["sosialhjelp-kommune-service-all.jar"]

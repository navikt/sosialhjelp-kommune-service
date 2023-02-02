FROM gcr.io/distroless/java17

# TODO change to match the path to your "fat jar"
COPY build/libs/app-all.jar /app/app-all.jar

WORKDIR /app

USER nonroot

CMD ["app-all.jar"]

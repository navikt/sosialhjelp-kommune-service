FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-27
ENV TZ="Europe/Oslo"
ENV JAVA_OPTS="-Dlogback.configurationFile=logback.xml"

COPY build/libs/sosialhjelp-kommune-service-all.jar /app/app.jar

WORKDIR /app

CMD ["-jar","app.jar"]

FROM eclipse-temurin:17-jdk

WORKDIR /graphhopper

COPY web/target/graphhopper-web-9.0-SNAPSHOT.jar web/target/
COPY config-example.yml .
COPY data/ data/

EXPOSE 8989

CMD ["java", "-jar", "web/target/graphhopper-web-9.0-SNAPSHOT.jar", "server", "config-example.yml"]

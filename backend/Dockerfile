FROM adoptopenjdk/openjdk14-openj9 AS builder
COPY ../ .
RUN ./gradlew backend:installBootDist -Dorg.gradle.daemon=false

FROM adoptopenjdk/openjdk14-openj9
WORKDIR /usr/app
COPY --from=builder backend/build/install/backend-boot .
ENTRYPOINT ["/usr/app/bin/backend"]

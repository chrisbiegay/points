FROM openjdk:11
WORKDIR /points

COPY gradle gradle
COPY build.gradle settings.gradle gradlew ./
COPY src src

ENTRYPOINT ["./gradlew"]
CMD ["bootRun"]
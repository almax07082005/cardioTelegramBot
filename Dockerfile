FROM amazoncorretto:21-alpine3.18-jdk
WORKDIR /app
COPY /build/libs/cardioBot.jar ./
ENTRYPOINT ["java", "-jar", "cardioBot.jar"]

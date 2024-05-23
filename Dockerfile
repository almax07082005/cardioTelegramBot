FROM amazoncorretto:20-alpine3.18-jdk
WORKDIR /app
COPY /build/libs/cardioBot.jar ./
ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "./cardioBot.jar"]

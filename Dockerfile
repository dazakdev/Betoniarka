FROM gradle:jdk25-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle clean build -x test --no-daemon


FROM openjdk:25-ea-jdk-slim
EXPOSE 8080
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/demo.jar
ENTRYPOINT ["java", "-jar", "/app/demo.jar"]

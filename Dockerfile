FROM maven:3.8.6-openjdk-11

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:resolve

COPY src src

RUN mvn clean package

EXPOSE 8000

COPY tmp tmp

RUN cp /app/target/TestServer-1.0-SNAPSHOT.jar .

ENTRYPOINT ["java", "-jar", "TestServer-1.0-SNAPSHOT.jar"]
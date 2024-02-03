FROM openjdk:17

WORKDIR /app

COPY target/demo-0.0.1-SNAPSHOT.jar /app

EXPOSE 9285

# Define environment variable
ENV NAME World

CMD ["java", "-jar", "demo-0.0.1-SNAPSHOT.jar"]

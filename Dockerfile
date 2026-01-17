FROM amazoncorretto:17-alpine-jdk

WORKDIR /app

COPY build/libs/snut-likelion-app.jar /app/snut-likelion-app.jar

# Expose the port that the application will run on
EXPOSE 8080

# Run the jar file when the container starts with Pinpoint agent
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/app/snut-likelion-app.jar"]
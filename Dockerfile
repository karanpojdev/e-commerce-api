# 1. Use a slim JDK image as the base (lighter than a full JRE)
FROM eclipse-temurin:17-jdk-alpine

# 2. Set the working directory inside the container
WORKDIR /app

# 3. Copy the compiled JAR file from your target directory 
#    Replace 'e-commerce-api-0.0.1-SNAPSHOT.jar' with your actual generated JAR name
ARG JAR_FILE=target/e-commerce-api-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# 4. Expose the port your Spring Boot app runs on
EXPOSE 8080

# 5. Define the entry point command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
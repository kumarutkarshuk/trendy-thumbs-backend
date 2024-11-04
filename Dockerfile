# Use the official Java 21 image as the base image
FROM openjdk:21

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file into the container
COPY target/trendy-thumbs-0.0.1-SNAPSHOT.jar trendy-thumbs.jar

# Run the JAR file with environment variables
ENTRYPOINT ["java", "-jar", "trendy-thumbs.jar"]
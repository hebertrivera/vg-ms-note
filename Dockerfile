#Use official OpenJDK 17 image as base
FROM openjdk:17-jdk-alpine

#Set working directory
WORKDIR /app

#Copy the packaged jar file into our docker image
COPY target/vg-ms-note-0.0.1-SNAPSHOT.jar app.jar

#Expose the port
EXPOSE 8093

#Specify the command to run on container start
CMD ["java", "-jar", "app.jar"]
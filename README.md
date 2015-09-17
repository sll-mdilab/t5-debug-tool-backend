# Debug Tool Backend

## Introduction

This is a server application which makes up the backend for a tool with the purpouse of helping to locate faults in the system. It lets an administrator see wether messages are entering the system and wether incoming requests from consuming systems are successful. The application reads messages and request logs from an XML-database and delivers these along with summarised information to the frontend.

## Build

The application is written in Java version 8 and uses Gradle for automatic building and dependency management.
Assumingthat Java SE/EE 8 development kit is installed and exist on the PATH environment variable, the project can be built with the following command from the project root folder:

    ./gradlew build

This outputs a .war-file into the `build/lib` directory.

## Deployment

The build process produces a servlet contained in a .war-file which can be deployed on any compatible Java servlet container. It has been tested with Apache Tomcat 8.0.

The application uses the following environment variables:

* `DT_DATABASE_HOST` - The IP/hostname of the database.
* `DT_DATABASE_PORT` - Port number of XCC endpoint.
* `DT_DATABASE_NAME` - Name of the database schema.
* `DT_DATABASE_USER` - Database username.
* `DT_DATABASE_PASSWORD` - Database password.

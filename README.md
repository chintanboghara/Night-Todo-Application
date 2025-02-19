# Night Todo Application

NightTodo is a sleek and modern dark-themed Todo application built with Java, Spring Boot, Maven, and Thymeleaf. It provides a simple interface to manage your daily tasks while demonstrating a robust multi-stage Docker build process for containerized deployment.

## Features

- **Add Tasks:** Quickly add new todo items.
- **Complete Tasks:** Mark tasks as completed with a single click.
- **Delete Tasks:** Remove tasks that are no longer needed.
- **Dark Themed UI:** Enjoy a visually appealing interface with a sleek black background.

## Technology Stack

- **Java 17**
- **Spring Boot 3.1.2**
- **Maven**
- **Thymeleaf**
- **Docker**

## Prerequisites

- **Java 17 or later**
- **Maven 3.8.5 or later**
- **Docker** (optional, for containerization)

## Running Locally

1. **Clone the repository:**

   ```bash
   git clone https://github.com/chintanboghara/Night-Todo-Application.git
   cd Night-Todo-Application
   ```

2. **Run the application using Maven:**

   ```bash
   mvn spring-boot:run
   ```

   The application will start and be accessible at [http://localhost:8081](http://localhost:8081).

### Building the Application JAR

To package the application as an executable JAR file, run:

```bash
mvn clean package
```

After the build, the packaged JAR will be located in the `target` directory (e.g., `target/todo-app-1.0.0.jar`).

## Docker

This project includes a multi-stage Dockerfile to build and run the application in a container.

### Building the Docker Image

From the project root, build the Docker image with:

```bash
docker build -t night-todo-application .
```

### Running the Docker Container

Run the Docker container by mapping the container's port to your host:

```bash
docker run -p 8081:8081 night-todo-application
```

The application will then be accessible at [http://localhost:8081](http://localhost:8081).

## Configuration

The application's configuration is managed in `src/main/resources/application.properties`. By default, the server is configured to run on port **8081**:

```properties
server.port=8081
```

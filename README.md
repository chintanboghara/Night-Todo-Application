[![Build and Push Docker Image](https://github.com/chintanboghara/Night-Todo-Application/actions/workflows/docker-publish.yml/badge.svg?branch=main)](https://github.com/chintanboghara/Night-Todo-Application/actions/workflows/docker-publish.yml)
[![Docker Image CI](https://github.com/chintanboghara/Night-Todo-Application/actions/workflows/docker-image.yml/badge.svg?branch=main)](https://github.com/chintanboghara/Night-Todo-Application/actions/workflows/docker-image.yml)
[![Java CI](https://github.com/chintanboghara/Night-Todo-Application/actions/workflows/Java%20CI.yml/badge.svg?branch=main)](https://github.com/chintanboghara/Night-Todo-Application/actions/workflows/Java%20CI.yml)

# Night Todo Application

Night Todo Application is a sleek, modern, and dark-themed Todo application built with Java, Spring Boot, Maven, and Thymeleaf. It offers a straightforward interface to manage your daily tasks and showcases a robust multi-stage Docker build process for containerized deployment.

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

- **Java 17** or later
- **Maven 3.8.5** or later
- **Docker** (optional, for containerization)
- **Docker Compose** (if using the provided `docker-compose.yml`)

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

## Building the Application JAR

To package the application as an executable JAR file, run:

```bash
mvn clean package
```

After the build, the packaged JAR will be located in the `target` directory (e.g., `target/todo-app-1.0.0.jar`).

## Docker

This project includes both a multi-stage `Dockerfile` and a `docker-compose.yml` file to build and run the application in a containerized environment.

### Using the Dockerfile

#### Building the Docker Image

From the project root, run:

```bash
docker build -t night-todo-application .
```

#### Running the Docker Container

Map the container's port to your host with:

```bash
docker run -p 8081:8081 night-todo-application
```

The application will be accessible at [http://localhost:8081](http://localhost:8081).

### Using Docker Compose

Docker Compose makes it even easier to manage the container lifecycle. From the project root, run:

```bash
docker-compose up --build
```

This command will build the image (if needed) and start the container, making the application available at [http://localhost:8081](http://localhost:8081). To stop the container, use:

```bash
docker-compose down
```

## Cloud Native Buildpacks

Build and run the Night Todo Application using Cloud Native Buildpacks.

### 1. Install the `pack` CLI

- **Download and Install:**  
  Visit the [Buildpacks CLI documentation](https://buildpacks.io/docs/tools/pack/) for instructions on installing the `pack` CLI on your operating system.
  ```bash
  sudo add-apt-repository ppa:cncf-buildpacks/pack-cli
  sudo apt-get update
  sudo apt-get install pack-cli
  ```
  ```bash
  # Windows 
  choco install pack --version=0.36.4
  ```
### 2. Build the Container Image

- **Navigate to Your Project Directory:**  
  Ensure you are in the root directory of your project (where your `pom.xml` is located).

- **Run the Buildpacks Command:**  
  Use the following command to create a container image. Cloud Native Buildpacks will automatically detect your Java Spring Boot application and build it accordingly.

  ```bash
  pack build night-todo-application --builder paketobuildpacks/builder:base
  ```

  **Notes:**
  - `night-todo-application` is the name you are assigning to your container image.
  - `--builder paketobuildpacks/builder:base` specifies the builder that supports Java applications. You can choose a different builder (like `paketobuildpacks/builder:tiny`) if you prefer a smaller image.

### 3. Run the Container

- **Start the Container Using Docker:**  
  Once the image is built, run it with Docker:

  ```bash
  docker run -p 8081:8081 night-todo-application
  ```

  **Explanation:**
  - This command maps port **8081** of the container to port **8081** on your host machine.
  - The application will be accessible at [http://localhost:8081](http://localhost:8081).

## Configuration

The application's configuration is managed in `src/main/resources/application.properties`. By default, the server is configured to run on port **8081**:

```properties
server.port=8081
```

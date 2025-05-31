[![Build and Push Docker Image](https://github.com/chintanboghara/Night-Todo-Application/actions/workflows/docker-publish.yml/badge.svg?branch=main)](https://github.com/chintanboghara/Night-Todo-Application/actions/workflows/docker-publish.yml)
[![Docker Image CI](https://github.com/chintanboghara/Night-Todo-Application/actions/workflows/docker-image.yml/badge.svg?branch=main)](https://github.com/chintanboghara/Night-Todo-Application/actions/workflows/docker-image.yml)
[![Java CI](https://github.com/chintanboghara/Night-Todo-Application/actions/workflows/Java%20CI.yml/badge.svg?branch=main)](https://github.com/chintanboghara/Night-Todo-Application/actions/workflows/Java%20CI.yml)

# Night Todo Application

Night Todo Application is a sleek, modern, and dark-themed Todo application built with Java, Spring Boot, Maven, and Thymeleaf. It offers a straightforward interface to manage your daily tasks and showcases a robust multi-stage Docker build process for containerized deployment.

## Features

- **Add Tasks:** Quickly add new todo items with titles, optional due dates, and priority levels.
- **Edit Tasks:** Modify the title, due date, and priority of existing tasks.
- **Complete Tasks:** Mark tasks as completed with a single click.
- **Delete Tasks:** Remove tasks that are no longer needed.
- **Due Dates:** Assign due dates to tasks to keep track of deadlines.
- **Visual Reminders for Due Dates:** Overdue tasks are visually highlighted in the task list.
- **Task Prioritization:** Assign priority levels (High, Medium, Low) to tasks.
- **Priority Display:** Task priorities are clearly displayed with color-coded badges.
- **Hierarchical Tasks (Subtasks):**
    - Break down complex tasks into smaller, manageable subtasks.
    - Add subtasks directly to any existing task.
    - Subtasks are displayed hierarchically indented under their parent task.
    - Subtasks can be managed (completed, edited, deleted) independently.
    - Deleting a parent task automatically deletes all its associated subtasks (cascade deletion).
- **Advanced Filtering and Sorting:** Easily find and organize tasks with comprehensive filtering and sorting options available directly on the main task view.
    - **Filter by Status:** View all tasks, or only 'Pending' or 'Completed' ones.
    - **Filter by Priority:** Focus on tasks based on their 'High', 'Medium', or 'Low' priority.
    - **Filter by Due Date:** Narrow down tasks by criteria like 'Overdue', 'Today', 'Next 7 Days', or a specific date.
    - **Search by Title:** Quickly find tasks by typing keywords from their title.
    - **Sort Tasks:** Arrange tasks by 'Creation Date', 'Due Date', 'Priority', or 'Title'.
    - **Sort Direction:** Choose between 'Ascending' or 'Descending' order for all sort options.
- **Drag-and-Drop Task Reordering:** Intuitively change the order of tasks by dragging and dropping them within their current list.
    - **Manual Sorting:** Easily set a custom display order for top-level tasks and for subtasks within their respective parent.
    - **Persistent Order:** Your custom task order is saved and will be remembered.
    - **Default View:** Tasks are displayed in your manually set order by default when no other column-specific sorting (like by due date or priority) is active.
    - **Interaction with Column Sorting:** If you sort tasks by a specific column (e.g., 'Due Date'), that sort takes precedence. However, for items with the same value in the sorted column (e.g., multiple tasks with the same due date), their relative manual order is maintained (stable sort).
- **Persistent Task Storage:** Tasks (including parent-child relationships) are saved in an H2 in-memory database, with console access enabled for development.
- **Dark Themed UI:** Enjoy a visually appealing interface with a sleek black background.

## Technology Stack

- **Java 17**
- **Spring Boot 3.1.2**
- **Spring Data JPA**
- **Maven**
- **Thymeleaf**
- **Thymeleaf Extras Java8Time** (for date formatting in templates)
- **SortableJS** (for drag-and-drop UI)
- **H2 Database**
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

This project is configured for containerization using Docker, providing a consistent environment for building and running the application.

- **Optimized Build Context:** A `.dockerignore` file is utilized to exclude unnecessary files (like `.git`, `target/`, IDE configurations) from the Docker build context, ensuring faster and leaner builds.

### Using the Dockerfile

The `Dockerfile` implements a multi-stage build strategy:
1.  A `builder` stage uses a Maven/JDK image (`maven:3.8.5-openjdk-17`) to compile the Java application and build the executable JAR. This stage leverages Docker's layer caching for dependencies by first copying the `pom.xml` and downloading dependencies before copying the source code.
2.  A final, lean runtime stage uses a JRE image (`openjdk:17-jre-alpine`) for a significantly reduced image size.
3.  For improved security, the application in the final stage runs as a non-root user (`appuser`).

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

The `docker-compose.yml` file provides a simple way to build and run the application service:
- It uses the local `Dockerfile` for building.
- An explicit image name (`night-todo-app-compose:latest`) is defined for the built image.
- It maps port `8081` on the host to port `8081` in the container.

To build and run with Docker Compose:
```bash
docker-compose up --build
```

The application will be accessible at [http://localhost:8081](http://localhost:8081). To stop and remove the container:

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

## Application Configuration

The application's configuration is managed in `src/main/resources/application.properties`.

### Server Port

By default, the server is configured to run on port **8081**:

```properties
server.port=8081
```

### Database (H2)

The application uses an H2 in-memory database by default.
- **H2 Console:** The H2 console is enabled for development and can be accessed at `http://localhost:8081/h2-console`.
- **JDBC URL:** `jdbc:h2:mem:testdb`
- **Username:** `sa`
- **Password:** `password`

These settings can be found and modified in `application.properties`:
```properties
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

**Persistent Storage (Optional):**
To persist data across application restarts (when running locally, not typically for containerized deployments without further Docker configuration), you can change the `spring.datasource.url` to use a file-based H2 database. For example:
```properties
# spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.url=jdbc:h2:file:./data/tododb
```
This will create a database file in a `data` directory in your project's root.

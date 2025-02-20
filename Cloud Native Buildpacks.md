# Cloud Native Buildpacks

Build and run the Night Todo Application using Cloud Native Buildpacks.

## 1. Install the `pack` CLI

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
## 2. Build the Container Image

- **Navigate to Your Project Directory:**  
  Ensure you are in the root directory of your project (where your `pom.xml` is located).

- **Run the Buildpacks Command:**  
  Use the following command to create a container image. Cloud Native Buildpacks will automatically detect your Java Spring Boot application and build it accordingly.

  ```bash
  pack build night-todo-app --builder paketobuildpacks/builder:base
  ```

  **Notes:**
  - `night-todo-app` is the name you are assigning to your container image.
  - `--builder paketobuildpacks/builder:base` specifies the builder that supports Java applications. You can choose a different builder (like `paketobuildpacks/builder:tiny`) if you prefer a smaller image.

## 3. Run the Container

- **Start the Container Using Docker:**  
  Once the image is built, run it with Docker:

  ```bash
  docker run -p 8081:8081 night-todo-app
  ```

  **Explanation:**
  - This command maps port **8081** of the container to port **8081** on your host machine.
  - The application will be accessible at [http://localhost:8081](http://localhost:8081).

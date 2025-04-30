# Ktor Kotlin App

## Getting Started

To start the application, follow these steps:

1. **Build the Docker image:**

   Run the following command to build the Docker image:

   ```bash
   docker-compose build

2. **Run the Docker container:**

   After creating the image, run the Docker container to start both the database and the application.
   You can use the command:

   ```bash
   docker-compose up
   ```

3. **Updating docker image for new changes:**

   After upating the project there might be an issue using the new changes with docker, run the following commands to resolve the issue:
   
   ```bash
   docker-compose down
   docker-compose build --no-cache
   docker-compose up
   ```
   
## Available API Endpoints

Once the app is running, the following two API calls will be available:

### 1. `GET http://0.0.0.0:8080/users?query=Name&limit=10`

#### Output:

- **If no users are present:**

  ```json
  {
    "users": [],
    "total": 0
  }

- **If users are present:**
 ```json
  {
      "users": [
        {
            "email": "email@gmail.com",
            "name": "Name Surname"
        }
    ],
    "total": 1
  }
```
### 2. `POST http://0.0.0.0:8080/users`

  #### Body:

   ```json
  {
    "name": "Name Surname",
    "email": "email@gmail.com",
    "password": "String_123"
  }
```

  #### Output:
  - 202 Accepted,
  - 404 Bad Request (if the email is already present a 400 Bad Request is returned).
```json
   {
      "error": "Duplicate e-mail: email@gmail.com"
  }
```

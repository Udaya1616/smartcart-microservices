# SmartCart E-Commerce Microservices

SmartCart is a backend e-commerce project built with Java 21, Spring Boot, Spring Cloud, MySQL, JWT security, Feign, and Resilience4j.

The project is split into small services. Each service has one clear job:

- `config-server` gives configuration to the services from `config-repo`.
- `api-gateway` is the single entry point for API calls.
- `user-service` handles registration, login, JWT tokens, and user lookup.
- `product-service` handles products and stock quantity.
- `order-service` places orders and calls the product service to check and reduce stock.

## Project Structure

```text
smartcart/
|-- api-gateway/        # Gateway service, runs on port 8080
|-- config-repo/        # YAML config files used by config-server
|-- config-server/      # Spring Cloud Config Server, runs on port 8888
|-- docs/               # SQL setup script and Postman collection
|-- order-service/      # Order APIs, runs on port 8083
|-- product-service/    # Product APIs, runs on port 8082
|-- user-service/       # User/auth APIs, runs on port 8081
|-- pom.xml             # Parent Maven project
`-- README.md
```

## Technology Used

| Area | Technology |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 3.3.7 |
| Microservices | Spring Cloud 2023.0.4 |
| API Gateway | Spring Cloud Gateway |
| Configuration | Spring Cloud Config Server |
| Database | MySQL |
| ORM | Spring Data JPA / Hibernate |
| Security | Spring Security, JWT |
| Service Calls | OpenFeign |
| Fault Tolerance | Resilience4j retry and circuit breaker |
| API Docs | Springdoc OpenAPI / Swagger UI |
| Build Tool | Maven |

## Features

### Architecture Features

- Microservice-based backend with separate services for users, products, orders, gateway, and configuration.
- Centralized service configuration using Spring Cloud Config Server.
- API Gateway on port `8080`, so clients do not need to call every service directly.
- Separate MySQL database for each business service.
- Service-to-service communication from `order-service` to `product-service` using OpenFeign.
- Retry and circuit breaker support in `order-service` using Resilience4j.
- Swagger UI available for API documentation and manual testing.
- Postman collection included for easier API testing.

### User and Security Features

- User registration.
- User login.
- JWT token generation after successful login.
- Role-based access with `USER` and `ADMIN` roles.
- Default admin account is created automatically when `user-service` starts.
- Passwords are stored in encoded form, not plain text.

### Product Features

- Add product.
- Update product.
- Delete product.
- View one product.
- View all products.
- Search products by keyword.
- Sort products by `id`, `name`, `price`, or `quantity`.
- Reduce product stock.
- Increase product stock.
- Admin-only product management.

### Order Features

- Place an order as a logged-in user.
- Validate that a user can place orders only for their own account.
- Check product availability before creating an order.
- Reduce stock after successful order creation.
- Calculate total order amount from product price and quantity.
- View logged-in user's order history.
- Admin can view all orders.
- Admin can view orders for a specific user.

### Fault Tolerance Features

- `order-service` calls `product-service` through Feign.
- If `product-service` is temporarily unavailable, `order-service` retries the call.
- If repeated calls fail, the circuit breaker opens and fallback logic returns a clear service unavailable error.
- This prevents `order-service` from continuously waiting on a failing product service.

## Service Ports

| Service | Port | Base URL |
| --- | ---: | --- |
| Config Server | 8888 | `http://localhost:8888` |
| API Gateway | 8080 | `http://localhost:8080` |
| User Service | 8081 | `http://localhost:8081` |
| Product Service | 8082 | `http://localhost:8082` |
| Order Service | 8083 | `http://localhost:8083` |

Use the API Gateway URL for normal testing:

```text
http://localhost:8080
```

## Database Design

This project uses three separate MySQL databases:

| Database | Table | Used By |
| --- | --- | --- |
| `smartcart_users` | `users` | `user-service` |
| `smartcart_products` | `products` | `product-service` |
| `smartcart_orders` | `orders` | `order-service` |

There are no database foreign keys between these databases because each microservice owns its own data. The order service stores `user_id` and `product_id` values and communicates with the product service through HTTP when placing an order.

## Database Setup Script

The database setup script is:

```text
docs/database-setup.sql
```

Run it in MySQL before starting the services:

```bash
mysql -u root -p < docs/database-setup.sql
```

The current project configuration reads MySQL credentials from environment variables:

```text
MYSQL_USERNAME
MYSQL_PASSWORD
```

If environment variables are not set, the services use these defaults:

```text
username: root
password: root
```

If your local MySQL password is different, set it before starting the services:

```bash
export MYSQL_USERNAME=root
export MYSQL_PASSWORD=your_mysql_password
```

## Required Software

Install these before running the project:

1. Java 21
2. Maven, or use the included Maven wrapper `./mvnw`
3. MySQL Server
4. Postman, optional but useful because the project includes `docs/SmartCart.postman_collection.json`

Check Java:

```bash
java -version
```

Check MySQL:

```bash
mysql --version
```

## Build the Project

From the project root folder:

```bash
./mvnw clean install
```

If `./mvnw` is not executable, run:

```bash
chmod +x mvnw
```

## Run the Project

Start the services in this exact order.

### 1. Start Config Server

```bash
./mvnw -pl config-server spring-boot:run
```

Wait until it starts on port `8888`.

### 2. Start User Service

Open a new terminal:

```bash
./mvnw -pl user-service spring-boot:run
```

This service creates a default admin user automatically if it does not already exist:

```text
username: admin
password: admin123
role: ADMIN
```

### 3. Start Product Service

Open a new terminal:

```bash
./mvnw -pl product-service spring-boot:run
```

### 4. Start Order Service

Open a new terminal:

```bash
./mvnw -pl order-service spring-boot:run
```

### 5. Start API Gateway

Open a new terminal:

```bash
./mvnw -pl api-gateway spring-boot:run
```

After all services are running, use:

```text
http://localhost:8080
```

## API Testing Flow

### Step 1: Login as Admin

Use this to get an admin JWT token:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Copy the `token` value from the response.

For protected APIs, send this header:

```text
Authorization: Bearer YOUR_TOKEN_HERE
```

### Step 2: Add a Product

Only an admin can add products.

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -d '{
    "name": "Wireless Mouse",
    "description": "Bluetooth wireless mouse",
    "price": 499.00,
    "quantity": 25
  }'
```

### Step 3: Register a Normal User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"user123"}'
```

### Step 4: Login as Normal User

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"user123"}'
```

Copy the user token from the response.

### Step 5: View Products

```bash
curl http://localhost:8080/api/products \
  -H "Authorization: Bearer YOUR_USER_TOKEN"
```

Optional query parameters:

```text
keyword=mouse
sortBy=id
sortBy=name
sortBy=price
sortBy=quantity
```

Example:

```bash
curl "http://localhost:8080/api/products?keyword=mouse&sortBy=price" \
  -H "Authorization: Bearer YOUR_USER_TOKEN"
```

### Step 6: Place an Order

The `userId` must match the logged-in user's token.

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_USER_TOKEN" \
  -d '{
    "userId": 2,
    "productId": 1,
    "quantity": 2
  }'
```

When an order is placed:

1. `order-service` checks the product by calling `product-service`.
2. `order-service` checks if enough stock is available.
3. `order-service` asks `product-service` to reduce stock.
4. `order-service` saves the order in `smartcart_orders.orders`.

## How to Test Retry and Circuit Breaker

Circuit breaker is implemented in `order-service` when it calls `product-service`.

The protected methods are:

- `getProductWithResilience`
- `reduceStockWithResilience`

Configuration is in:

```text
config-repo/order-service.yml
```

Current settings:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      productService:
        sliding-window-size: 5
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10s
  retry:
    instances:
      productService:
        max-attempts: 3
        wait-duration: 1s
```

This means:

- Resilience4j watches the last `5` product-service calls.
- If at least `50%` of those calls fail, the circuit breaker opens.
- When the circuit is open, calls fail fast instead of repeatedly waiting for `product-service`.
- After `10` seconds, it allows test calls again.
- Each failed product-service call is retried up to `3` times with `1` second between attempts.

### Circuit Breaker Test Steps

Start these services first. Use a separate terminal for each service:

```bash
./mvnw -pl config-server spring-boot:run
./mvnw -pl user-service spring-boot:run
./mvnw -pl product-service spring-boot:run
./mvnw -pl order-service spring-boot:run
./mvnw -pl api-gateway spring-boot:run
```

Login as admin and add a product if you do not already have one:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -d '{
    "name": "Circuit Breaker Test Product",
    "description": "Product used for resilience testing",
    "price": 100.00,
    "quantity": 20
  }'
```

Register and login as a normal user:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"cbuser","password":"user123"}'
```

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"cbuser","password":"user123"}'
```

Now stop only `product-service`. Keep these services running:

```text
config-server
user-service
order-service
api-gateway
```

Send the order request multiple times using the normal user's token.

Replace `userId` with the user id returned by login/register. Replace `productId` with an existing product id.

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_USER_TOKEN" \
  -d '{
    "userId": 2,
    "productId": 1,
    "quantity": 1
  }'
```

Expected result:

- The order will not be saved.
- `order-service` will retry the product call.
- After repeated failures, fallback logic runs.
- The response will show a service unavailable style error, for example:

```text
Product service is unavailable. Try again later.
```

or:

```text
Unable to update product stock. Try again later.
```

In the `order-service` logs, you should see warning logs like:

```text
Product service lookup failed for product 1
Product stock update failed for product 1
```

Start `product-service` again and wait around `10` seconds:

```bash
./mvnw -pl product-service spring-boot:run
```

Send the same order request again. If the product exists and has enough quantity, the order should succeed.

## Main API Endpoints

### Auth APIs

| Method | URL | Access | Purpose |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | Public | Register a normal user |
| `POST` | `/api/auth/login` | Public | Login and get JWT token |

### User APIs

| Method | URL | Access | Purpose |
| --- | --- | --- | --- |
| `GET` | `/api/users/{id}` | Public in current code | Get user details |

### Product APIs

| Method | URL | Access | Purpose |
| --- | --- | --- | --- |
| `GET` | `/api/products` | USER or ADMIN | List products |
| `GET` | `/api/products/{id}` | USER or ADMIN | Get one product |
| `POST` | `/api/products` | ADMIN only | Add product |
| `PUT` | `/api/products/{id}` | ADMIN only | Update product |
| `DELETE` | `/api/products/{id}` | ADMIN only | Delete product |
| `PUT` | `/api/products/{id}/stock/reduce` | ADMIN only | Reduce or increase stock |

Stock update request body:

```json
{
  "quantity": 5,
  "operation": "REDUCE"
}
```

Allowed `operation` values:

```text
REDUCE
INCREASE
```

If `operation` is not sent, the service uses `REDUCE`.

### Order APIs

| Method | URL | Access | Purpose |
| --- | --- | --- | --- |
| `POST` | `/api/orders` | USER only | Place order for logged-in user |
| `GET` | `/api/orders/history` | USER only | View logged-in user's orders |
| `GET` | `/api/orders/user/{userId}` | ADMIN only | View a user's orders |
| `GET` | `/api/orders` | ADMIN only | View all orders |

## Swagger UI

After services start, open these URLs in a browser:

| Service | Swagger URL |
| --- | --- |
| API Gateway | `http://localhost:8080/swagger-ui.html` |
| User Service | `http://localhost:8081/swagger-ui.html` |
| Product Service | `http://localhost:8082/swagger-ui.html` |
| Order Service | `http://localhost:8083/swagger-ui.html` |

## Postman Collection

A Postman collection is included here:

```text
docs/SmartCart.postman_collection.json
```

Import this file into Postman and test the APIs through:

```text
http://localhost:8080
```

## Important Notes

- Start `config-server` first because the services read their configuration from it.
- Start `product-service` before placing orders because `order-service` calls it.
- Public registration always creates users with role `USER`.
- Admin user is created automatically by `user-service` using `admin / admin123`.
- JWT token expiration is configured as `86400000` milliseconds, which is 24 hours.
- The services also have `ddl-auto: update`, so Hibernate can update tables automatically, but the SQL script is provided so the database structure is clear and can be created manually.

## Common Problems

### MySQL login fails

Check the password in:

```text
config-repo/user-service.yml
config-repo/product-service.yml
config-repo/order-service.yml
```

Make sure it matches your local MySQL root password.

### Port already in use

Stop the program using that port or change the port in the related YAML file.

### Product APIs return unauthorized

Login first and pass the JWT token:

```text
Authorization: Bearer YOUR_TOKEN_HERE
```

### Order creation fails

Check these points:

1. `product-service` is running on port `8082`.
2. The product exists.
3. The product has enough quantity.
4. The `userId` in the request matches the logged-in user's token.
